package com.invoiceflow.api.service;

import com.invoiceflow.api.model.Invoice;
import com.invoiceflow.api.model.InvoiceStatus;
import com.invoiceflow.api.repository.InvoiceRepository;
import com.invoiceflow.api.web.dto.InboundInvoiceRequest;
import com.invoiceflow.api.web.dto.StatsResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceService {

    /** Below this confidence an invoice is routed to human review instead of auto-processing. */
    public static final double REVIEW_THRESHOLD = 0.90;

    /** Manual entry time a bookkeeper needs per invoice; used for the minutes-saved estimate. */
    private static final int MINUTES_PER_INVOICE = 8;

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Invoice ingest(InboundInvoiceRequest request) {
        // Normalise once so the duplicate check and the stored value always agree.
        String invoiceNumber = request.invoiceNumber().trim();
        repository.findByInvoiceNumber(invoiceNumber).ifPresent(existing -> {
            throw alreadyIngested(invoiceNumber);
        });

        InvoiceStatus status = request.extractionConfidence() >= REVIEW_THRESHOLD
                ? InvoiceStatus.PROCESSED
                : InvoiceStatus.NEEDS_REVIEW;

        Invoice invoice = new Invoice(
                request.vendorName().trim(),
                invoiceNumber,
                request.amount(),
                request.currency().toUpperCase(),
                request.issueDate(),
                request.dueDate(),
                status,
                request.extractionConfidence(),
                request.extractionModel(),
                request.sourceMessageId(),
                Instant.now(),
                status == InvoiceStatus.PROCESSED ? Instant.now() : null
        );
        try {
            // Flush now so a concurrent insert of the same number fails here, not at commit.
            return repository.saveAndFlush(invoice);
        } catch (DataIntegrityViolationException e) {
            // Another request inserted the same number between our check and our insert.
            throw alreadyIngested(invoiceNumber);
        }
    }

    private static ResponseStatusException alreadyIngested(String invoiceNumber) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "Invoice %s already ingested".formatted(invoiceNumber));
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAll(InvoiceStatus status) {
        return status == null
                ? repository.findAllByOrderByReceivedAtDesc()
                : repository.findByStatusOrderByReceivedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public Invoice get(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice %d not found".formatted(id)));
    }

    @Transactional
    public Invoice approve(Long id) {
        Invoice invoice = get(id);
        if (invoice.getStatus() != InvoiceStatus.NEEDS_REVIEW) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only invoices in NEEDS_REVIEW can be approved (current: %s)".formatted(invoice.getStatus()));
        }
        invoice.setStatus(InvoiceStatus.APPROVED);
        invoice.setProcessedAt(Instant.now());
        return repository.save(invoice);
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        List<Invoice> all = repository.findAll();
        long total = all.size();
        long auto = all.stream().filter(i -> i.getStatus() != InvoiceStatus.NEEDS_REVIEW).count();
        long review = total - auto;
        double avgConfidence = all.stream()
                .filter(i -> i.getExtractionConfidence() != null)
                .mapToDouble(Invoice::getExtractionConfidence)
                .average().orElse(0);

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (InvoiceStatus s : InvoiceStatus.values()) {
            byStatus.put(s.name(), all.stream().filter(i -> i.getStatus() == s).count());
        }
        Map<String, BigDecimal> byCurrency = new LinkedHashMap<>();
        all.forEach(i -> byCurrency.merge(i.getCurrency(), i.getAmount(), BigDecimal::add));

        return new StatsResponse(
                total,
                auto,
                review,
                total == 0 ? 0 : (double) auto / total,
                avgConfidence,
                auto * MINUTES_PER_INVOICE,
                byStatus,
                byCurrency
        );
    }
}

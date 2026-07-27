package com.invoiceflow.api.web;

import com.invoiceflow.api.model.Invoice;
import com.invoiceflow.api.model.InvoiceStatus;
import com.invoiceflow.api.service.InvoiceService;
import com.invoiceflow.api.web.dto.InboundInvoiceRequest;
import com.invoiceflow.api.web.dto.StatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Invoices", description = "Inbound processing, review workflow and pipeline statistics")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @PostMapping("/invoices/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ingest an extracted invoice",
            description = "Called by the n8n workflow after the AI extraction step. "
                    + "Invoices with confidence >= 0.90 are auto-processed; lower confidence is routed to human review.")
    public Invoice ingest(@Valid @RequestBody InboundInvoiceRequest request) {
        return service.ingest(request);
    }

    @GetMapping("/invoices")
    @Operation(summary = "List invoices", description = "Most recent first; optional status filter.")
    public List<Invoice> list(
            @Parameter(description = "Filter by pipeline status") @RequestParam(required = false) InvoiceStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/invoices/{id}")
    @Operation(summary = "Get one invoice")
    public Invoice get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/invoices/{id}/approve")
    @Operation(summary = "Approve a reviewed invoice",
            description = "Moves an invoice from NEEDS_REVIEW to APPROVED after a human check.")
    public Invoice approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @GetMapping("/stats")
    @Operation(summary = "Pipeline statistics", description = "Aggregates that power the InvoiceFlow dashboard.")
    public StatsResponse stats() {
        return service.stats();
    }
}

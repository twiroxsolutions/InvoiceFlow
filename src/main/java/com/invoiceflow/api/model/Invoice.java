package com.invoiceflow.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * An invoice extracted from an inbound email by the n8n + OpenAI pipeline.
 * All sample data is fictional.
 */
@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    private LocalDate issueDate;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    /** Confidence score reported by the AI extraction step (0.0 - 1.0). */
    private Double extractionConfidence;

    /** Model used by the extraction step, e.g. gpt-4o-mini. */
    private String extractionModel;

    /** Message-Id of the source email, for traceability. */
    private String sourceMessageId;

    private Instant receivedAt;
    private Instant processedAt;

    protected Invoice() {
    }

    public Invoice(String vendorName, String invoiceNumber, BigDecimal amount, String currency,
                   LocalDate issueDate, LocalDate dueDate, InvoiceStatus status,
                   Double extractionConfidence, String extractionModel,
                   String sourceMessageId, Instant receivedAt, Instant processedAt) {
        this.vendorName = vendorName;
        this.invoiceNumber = invoiceNumber;
        this.amount = amount;
        this.currency = currency;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.extractionConfidence = extractionConfidence;
        this.extractionModel = extractionModel;
        this.sourceMessageId = sourceMessageId;
        this.receivedAt = receivedAt;
        this.processedAt = processedAt;
    }

    public Long getId() { return id; }
    public String getVendorName() { return vendorName; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public InvoiceStatus getStatus() { return status; }
    public Double getExtractionConfidence() { return extractionConfidence; }
    public String getExtractionModel() { return extractionModel; }
    public String getSourceMessageId() { return sourceMessageId; }
    public Instant getReceivedAt() { return receivedAt; }
    public Instant getProcessedAt() { return processedAt; }

    public void setStatus(InvoiceStatus status) { this.status = status; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}

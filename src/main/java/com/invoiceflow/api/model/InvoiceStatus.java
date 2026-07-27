package com.invoiceflow.api.model;

public enum InvoiceStatus {
    /** Extracted with high confidence and stored automatically. */
    PROCESSED,
    /** Extraction confidence below threshold - waiting for a human check. */
    NEEDS_REVIEW,
    /** Reviewed and approved for payment. */
    APPROVED,
    /** Payment completed. */
    PAID
}

package com.invoiceflow.api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "Pipeline statistics for the dashboard")
public record StatsResponse(
        long totalInvoices,
        long autoProcessed,
        long needsReview,
        @Schema(description = "Share of invoices stored without human touch, 0-1") double automationRate,
        @Schema(description = "Average AI extraction confidence, 0-1") double avgConfidence,
        @Schema(description = "Estimated minutes of manual data entry avoided (8 min per auto-processed invoice)") long minutesSaved,
        Map<String, Long> byStatus,
        Map<String, BigDecimal> totalByCurrency
) {
}

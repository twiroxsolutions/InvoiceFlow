package com.invoiceflow.api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payload posted by the n8n workflow after the AI extraction step.
 */
@Schema(description = "Invoice fields extracted from an inbound email by the AI step")
public record InboundInvoiceRequest(

        @NotBlank
        @Schema(example = "Northpine Supplies Ltd")
        String vendorName,

        @NotBlank
        @Schema(example = "NP-2026-0187")
        String invoiceNumber,

        @NotNull @DecimalMin("0.01")
        @Schema(example = "1840.50")
        BigDecimal amount,

        @NotBlank @Size(min = 3, max = 3)
        @Schema(example = "USD")
        String currency,

        @Schema(example = "2026-07-14")
        LocalDate issueDate,

        @Schema(example = "2026-08-13")
        LocalDate dueDate,

        @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
        @Schema(description = "Confidence reported by the extraction model", example = "0.97")
        Double extractionConfidence,

        @Schema(example = "gpt-4.1-mini")
        String extractionModel,

        @Schema(description = "Message-Id of the source email", example = "<a1b2c3@mail.example>")
        String sourceMessageId
) {
}

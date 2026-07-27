package com.invoiceflow.api.config;

import com.invoiceflow.api.model.Invoice;
import com.invoiceflow.api.model.InvoiceStatus;
import com.invoiceflow.api.repository.InvoiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

/**
 * Seeds the database with FICTIONAL sample invoices. Vendor names are invented;
 * any resemblance to a real company is coincidental.
 */
@Configuration
public class SeedData {

    private record Vendor(String name, String prefix, String currency) {
    }

    // Each fictional vendor bills in ONE currency - avoids the synthetic-data tell
    // of a vendor switching currencies between invoices.
    private static final List<Vendor> VENDORS = List.of(
            new Vendor("Northpine Supplies Ltd", "NP", "USD"),
            new Vendor("Bramwick Logistics", "BRW", "USD"),
            new Vendor("Astervale Consulting", "AVC", "USD"),
            new Vendor("Quantic Print Co", "QP", "EUR"),
            new Vendor("Harborlight Media", "HLM", "EUR"),
            new Vendor("Veldtline Office Interiors", "VOI", "GBP"),
            new Vendor("Marrowgate Catering", "MG", "EUR"),
            new Vendor("Cloudmere Hosting", "CMH", "USD"),
            new Vendor("Fernwick Stationery", "FW", "USD"),
            new Vendor("Ospreypeak Travel", "OPT", "EUR")
    );

    @Bean
    CommandLineRunner seed(InvoiceRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            Random random = new Random(20260726L); // fixed seed -> reproducible sample data
            Instant now = Instant.now();

            for (int i = 0; i < 32; i++) {
                Vendor vendor = VENDORS.get(i % VENDORS.size());
                double confidence = switch (i % 8) {
                    case 3 -> 0.82 + random.nextDouble() * 0.07;  // ~1 in 8 lands in review
                    default -> 0.91 + random.nextDouble() * 0.08;
                };
                confidence = Math.round(confidence * 100.0) / 100.0;

                InvoiceStatus status;
                if (confidence < 0.90) {
                    status = (i < 16) ? InvoiceStatus.NEEDS_REVIEW : InvoiceStatus.APPROVED;
                } else if (i % 5 == 0) {
                    status = InvoiceStatus.PAID;
                } else {
                    status = InvoiceStatus.PROCESSED;
                }

                Instant received = now.minus(2L + i * 5L, ChronoUnit.HOURS)
                        .minus(random.nextInt(50), ChronoUnit.MINUTES);
                LocalDate issue = LocalDate.now().minusDays(1 + i * 2L);
                BigDecimal amount = BigDecimal.valueOf(120 + random.nextInt(4800) + random.nextInt(100) / 100.0)
                        .setScale(2, java.math.RoundingMode.HALF_UP);

                repository.save(new Invoice(
                        vendor.name(),
                        "%s-2026-%04d".formatted(vendor.prefix(), 140 + i * 3),
                        amount,
                        vendor.currency(),
                        issue,
                        issue.plusDays(30),
                        status,
                        confidence,
                        "gpt-4.1-mini",
                        "<%08x@mail.invoiceflow.local>".formatted(random.nextInt()),
                        received,
                        status == InvoiceStatus.NEEDS_REVIEW ? null : received.plus(38, ChronoUnit.SECONDS)
                ));
            }
        };
    }
}

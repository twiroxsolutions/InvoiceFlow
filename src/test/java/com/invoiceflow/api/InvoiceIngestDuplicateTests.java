package com.invoiceflow.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceIngestDuplicateTests {

    @Autowired
    private MockMvc mockMvc;

    private static String body(String invoiceNumber) {
        return """
                {"vendorName":"Northpine Supplies Ltd","invoiceNumber":"%s","amount":1840.50,
                 "currency":"USD","extractionConfidence":0.97}
                """.formatted(invoiceNumber);
    }

    private int ingest(String invoiceNumber) throws Exception {
        return mockMvc.perform(post("/api/invoices/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(invoiceNumber)))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void duplicateWithSurroundingWhitespaceIsRejectedWith409() throws Exception {
        mockMvc.perform(post("/api/invoices/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("TEST-WS-0140")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.invoiceNumber").value("TEST-WS-0140"));

        assertThat(ingest("TEST-WS-0140 ")).isEqualTo(409);
        assertThat(ingest("  TEST-WS-0140")).isEqualTo(409);
    }

    @Test
    void concurrentDuplicatesYieldOne201AndRest409() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                // Mix whitespace variants so both fixes are exercised together.
                String number = i % 2 == 0 ? "TEST-RACE-0001" : "TEST-RACE-0001 ";
                results.add(pool.submit(() -> {
                    start.await();
                    return ingest(number);
                }));
            }
            start.countDown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : results) {
                statuses.add(f.get());
            }
            assertThat(statuses).filteredOn(s -> s == 201).hasSize(1);
            assertThat(statuses).filteredOn(s -> s == 409).hasSize(threads - 1);
        } finally {
            pool.shutdownNow();
        }
    }
}

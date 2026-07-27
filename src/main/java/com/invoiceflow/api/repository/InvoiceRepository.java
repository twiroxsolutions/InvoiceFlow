package com.invoiceflow.api.repository;

import com.invoiceflow.api.model.Invoice;
import com.invoiceflow.api.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStatusOrderByReceivedAtDesc(InvoiceStatus status);

    List<Invoice> findAllByOrderByReceivedAtDesc();

    long countByStatus(InvoiceStatus status);
}

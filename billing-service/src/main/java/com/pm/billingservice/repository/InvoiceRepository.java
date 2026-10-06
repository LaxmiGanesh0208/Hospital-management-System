package com.pm.billingservice.repository;

import com.pm.billingservice.model.Invoice;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  List<Invoice> findByPatientEmailIgnoreCaseOrderByCreatedAtDesc(String patientEmail);
  Optional<Invoice> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
  Optional<Invoice> findByIdAndPatientEmailIgnoreCase(UUID id, String email);
}

package com.pm.billingservice.service;

import com.pm.billingservice.dto.InvoiceRequest;
import com.pm.billingservice.model.Invoice;
import com.pm.billingservice.repository.InvoiceRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InvoiceService {
  private final InvoiceRepository repository;
  private final String internalToken;
  public InvoiceService(InvoiceRepository repository, @Value("${billing.internal-service-token:}") String internalToken) {
    this.repository = repository; this.internalToken = internalToken;
  }
  @Transactional
  public Invoice create(InvoiceRequest request) {
    String currency = request.currency() == null || request.currency().isBlank() ? "INR" : request.currency().toUpperCase();
    if (!List.of("INR", "USD", "EUR").contains(currency)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency");
    var existing = repository.findByReferenceTypeAndReferenceId(request.referenceType(), request.referenceId());
    if (existing.isPresent()) {
      Invoice invoice = existing.get();
      if (!invoice.getPatientEmail().equalsIgnoreCase(request.patientEmail()) ||
          invoice.getAmount().compareTo(request.amount()) != 0 || !invoice.getCurrency().equals(currency))
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice reference already exists with different details");
      return invoice;
    }
    Invoice invoice = new Invoice(); invoice.setPatientEmail(request.patientEmail().trim().toLowerCase());
    invoice.setReferenceType(request.referenceType().trim().toUpperCase()); invoice.setReferenceId(request.referenceId().trim());
    invoice.setDescription(request.description().trim()); invoice.setAmount(request.amount()); invoice.setCurrency(currency);
    return repository.save(invoice);
  }
  public void authorizeInternal(String supplied) {
    if (internalToken.isBlank() || supplied == null || !MessageDigest.isEqual(
        internalToken.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8)))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal service authentication failed");
  }
  public List<Invoice> mine(String email) { return repository.findByPatientEmailIgnoreCaseOrderByCreatedAtDesc(email); }
  public List<Invoice> all() { return repository.findAll(); }
  public Invoice getMine(UUID id, String email) {
    return repository.findByIdAndPatientEmailIgnoreCase(id, email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
  }
  public Invoice get(UUID id) { return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found")); }
}

package com.pm.billingservice.controller;

import com.pm.billingservice.dto.InvoiceRequest;
import com.pm.billingservice.model.Invoice;
import com.pm.billingservice.service.InvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/billing")
public class InvoiceController {
  private final InvoiceService service;
  private final String paymentProvider;
  public InvoiceController(InvoiceService service,
      @Value("${billing.payment-provider:none}") String paymentProvider) {
    this.service = service; this.paymentProvider = paymentProvider;
  }

  @GetMapping("/payment-options")
  public Map<String, Object> paymentOptions() {
    boolean enabled = paymentProvider != null && !paymentProvider.equalsIgnoreCase("none");
    return Map.of("provider", enabled ? paymentProvider : "none", "enabled", enabled, "currency", "INR");
  }

  @PostMapping("/internal/invoices")
  @ResponseStatus(HttpStatus.CREATED)
  public Invoice createFromService(@RequestHeader(value = "X-Internal-Service-Token", required = false) String secret,
      @Valid @RequestBody InvoiceRequest request) {
    service.authorizeInternal(secret); return service.create(request);
  }

  @PostMapping("/invoices")
  @ResponseStatus(HttpStatus.CREATED)
  public Invoice createManual(@RequestHeader(value = "X-Authenticated-Role", required = false) String role,
      @Valid @RequestBody InvoiceRequest request) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "BILLING_STAFF"); return service.create(request);
  }

  @GetMapping("/invoices/my")
  public List<Invoice> mine(@RequestHeader(value = "X-Authenticated-Email", required = false) String email) {
    if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return service.mine(email);
  }

  @GetMapping("/invoices")
  public List<Invoice> all(@RequestHeader(value = "X-Authenticated-Role", required = false) String role) {
    requireAnyRole(role, "ADMIN", "SUPER_ADMIN", "BILLING_STAFF"); return service.all();
  }

  @GetMapping("/invoices/{id}")
  public Invoice get(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role, @PathVariable UUID id) {
    return isAdmin(role) ? service.get(id) : service.getMine(id, requiredEmail(email));
  }

  @GetMapping(value = "/invoices/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> download(@RequestHeader(value = "X-Authenticated-Email", required = false) String email,
      @RequestHeader(value = "X-Authenticated-Role", required = false) String role, @PathVariable UUID id) {
    Invoice invoice = isAdmin(role) ? service.get(id) : service.getMine(id, requiredEmail(email));
    byte[] pdf = invoicePdf(invoice);
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=invoice-" + invoice.getId() + ".pdf").contentType(MediaType.APPLICATION_PDF).body(pdf);
  }

  private static String requiredEmail(String email) {
    if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    return email;
  }
  private static boolean isAdmin(String role) { return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role) || "BILLING_STAFF".equals(role); }
  private static void requireAnyRole(String role, String... allowed) {
    for (String allowedRole : allowed) if (allowedRole.equals(role)) return;
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Billing staff role required");
  }
  private static byte[] invoicePdf(Invoice invoice) {
    String[] lines = {"MEDICARE+ - INVOICE", "Invoice: " + invoice.getId(), "Date: " + invoice.getCreatedAt(),
        "Patient: " + invoice.getPatientEmail(), "Description: " + invoice.getDescription(),
        "Reference: " + invoice.getReferenceType() + " / " + invoice.getReferenceId(),
        "Status: " + invoice.getStatus(), "Amount: " + invoice.getCurrency() + " " + invoice.getAmount()};
    StringBuilder stream = new StringBuilder("BT /F1 12 Tf 50 790 Td 16 TL\n");
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) stream.append("T*\n");
      stream.append('(').append(escapePdf(lines[i])).append(") Tj\n");
    }
    stream.append("ET");
    String content = stream.toString();
    String[] objects = {"<< /Type /Catalog /Pages 2 0 R >>", "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
        "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
        "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
        "<< /Length " + content.getBytes(java.nio.charset.StandardCharsets.US_ASCII).length + " >>\nstream\n" + content + "\nendstream"};
    StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
    int[] offsets = new int[objects.length + 1];
    for (int i = 0; i < objects.length; i++) { offsets[i + 1] = pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII).length; pdf.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n"); }
    int xref = pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII).length;
    pdf.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
    for (int i = 1; i < offsets.length; i++) pdf.append(String.format("%010d 00000 n \n", offsets[i]));
    pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");
    return pdf.toString().getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  }
  private static String escapePdf(String value) { return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replaceAll("[^\\x20-\\x7E]", "?"); }
}

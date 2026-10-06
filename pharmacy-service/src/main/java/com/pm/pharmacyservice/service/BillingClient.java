package com.pm.pharmacyservice.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class BillingClient {
  private final RestClient client;
  private final String token;
  public BillingClient(@Value("${billing.service.url:http://billing-service:4001}") String baseUrl,
      @Value("${billing.internal-service-token:}") String token) {
    this.client = RestClient.builder().baseUrl(baseUrl).build(); this.token = token;
  }
  public void createInvoice(String email, String orderId, BigDecimal amount) {
    if (token.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Billing integration is not configured");
    var request = new InvoiceCommand(email, "PHARMACY_ORDER", orderId, "Pharmacy order " + orderId, amount, "INR");
    try {
      client.post().uri("/billing/internal/invoices").header("X-Internal-Service-Token", token)
          .body(request).retrieve().toBodilessEntity();
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not create the billing invoice", ex);
    }
  }
  private record InvoiceCommand(String patientEmail, String referenceType, String referenceId,
      String description, BigDecimal amount, String currency) {}
}

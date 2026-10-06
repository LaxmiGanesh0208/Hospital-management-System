package com.pm.billingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"reference_type", "reference_id"}))
public class Invoice {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private String referenceType;
  @Column(nullable = false) private String referenceId;
  @Column(nullable = false) private String description;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
  @Column(nullable = false, length = 3) private String currency = "INR";
  @Column(nullable = false) private String status = "UNPAID";
  @Column(nullable = false) private Instant createdAt;
  @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getReferenceType() { return referenceType; }
  public void setReferenceType(String v) { referenceType = v; }
  public String getReferenceId() { return referenceId; }
  public void setReferenceId(String v) { referenceId = v; }
  public String getDescription() { return description; }
  public void setDescription(String v) { description = v; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal v) { amount = v; }
  public String getCurrency() { return currency; }
  public void setCurrency(String v) { currency = v; }
  public String getStatus() { return status; }
  public void setStatus(String v) { status = v; }
  public Instant getCreatedAt() { return createdAt; }
}

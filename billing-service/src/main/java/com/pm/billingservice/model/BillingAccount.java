package com.pm.billingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "billing_accounts")
public class BillingAccount {
  @Id private UUID id;
  @Column(nullable = false, unique = true) private String patientId;
  @Column(nullable = false) private String patientName;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private String status = "ACTIVE";
  @Column(nullable = false) private Instant createdAt;
  @PrePersist void onCreate() { if (id == null) id = UUID.randomUUID(); if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getPatientId() { return patientId; }
  public void setPatientId(String v) { patientId = v; }
  public String getPatientName() { return patientName; }
  public void setPatientName(String v) { patientName = v; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getStatus() { return status; }
}

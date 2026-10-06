package com.pm.patientservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "patient_medical_records")
public class MedicalRecord {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private String recordType;
  @Column(nullable = false) private String title;
  @Column(nullable = false, length = 4000) private String details;
  private String provider;
  @Column(nullable = false) private Instant recordedAt;
  @PrePersist void onCreate() { if (recordedAt == null) recordedAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getRecordType() { return recordType; }
  public void setRecordType(String v) { recordType = v; }
  public String getTitle() { return title; }
  public void setTitle(String v) { title = v; }
  public String getDetails() { return details; }
  public void setDetails(String v) { details = v; }
  public String getProvider() { return provider; }
  public void setProvider(String v) { provider = v; }
  public Instant getRecordedAt() { return recordedAt; }
}

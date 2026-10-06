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

@Entity @Table(name = "patient_notifications")
public class PatientNotification {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private String type;
  @Column(nullable = false) private String title;
  @Column(nullable = false, length = 1000) private String message;
  @Column(nullable = false) private boolean read;
  @Column(nullable = false) private Instant createdAt;
  @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getType() { return type; }
  public void setType(String v) { type = v; }
  public String getTitle() { return title; }
  public void setTitle(String v) { title = v; }
  public String getMessage() { return message; }
  public void setMessage(String v) { message = v; }
  public boolean isRead() { return read; }
  public void setRead(boolean v) { read = v; }
  public Instant getCreatedAt() { return createdAt; }
}

package com.pm.careservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "doctor_patient_assignments")
public class DoctorPatientAssignment {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID doctorId;
  @Column(nullable = false) private String doctorName;
  @Column(nullable = false) private String patientEmail;
  @Column(length = 1000) private String note;
  @Column(nullable = false) private String status = "PENDING_REVIEW";
  @com.fasterxml.jackson.annotation.JsonIgnore @Column(nullable = false) private String submittedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore private String reviewedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore private String reviewNote;
  @Column(nullable = false, updatable = false) private Instant createdAt;
  @PrePersist void created() { createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public UUID getDoctorId() { return doctorId; }
  public void setDoctorId(UUID v) { doctorId = v; }
  public String getDoctorName() { return doctorName; }
  public void setDoctorName(String v) { doctorName = v; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public String getNote() { return note; }
  public void setNote(String v) { note = v; }
  public String getStatus() { return status; }
  public void setStatus(String v) { status = v; }
  public String getSubmittedBy() { return submittedBy; }
  public void setSubmittedBy(String v) { submittedBy = v; }
  public String getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(String v) { reviewedBy = v; }
  public String getReviewNote() { return reviewNote; }
  public void setReviewNote(String v) { reviewNote = v; }
  public Instant getCreatedAt() { return createdAt; }
}

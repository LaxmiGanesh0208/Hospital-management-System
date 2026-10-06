package com.pm.careservice.model;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity @Table(name = "lab_bookings", uniqueConstraints = @UniqueConstraint(columnNames = {"test_id", "date", "time"}))
public class LabBooking {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID testId;
  @Column(nullable = false) private String testName;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private LocalDate date;
  @Column(nullable = false) private LocalTime time;
  @Column(nullable = false) private String collectionMethod;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
  @Column(nullable = false) private String status = "BOOKED";
  @Column(length = 4000) private String resultSummary;
  @com.fasterxml.jackson.annotation.JsonIgnore private String resultSubmittedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore private String resultReviewedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore @Column(length = 1000) private String resultReviewNote;
  @Column(nullable = false) private Instant createdAt;
  @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public UUID getTestId() { return testId; }
  public void setTestId(UUID v) { testId = v; }
  public String getTestName() { return testName; }
  public void setTestName(String v) { testName = v; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate v) { date = v; }
  public LocalTime getTime() { return time; }
  public void setTime(LocalTime v) { time = v; }
  public String getCollectionMethod() { return collectionMethod; }
  public void setCollectionMethod(String v) { collectionMethod = v; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal v) { price = v; }
  public String getStatus() { return status; }
  public void setStatus(String v) { status = v; }
  public String getResultSummary() { return resultSummary; }
  public void setResultSummary(String v) { resultSummary = v; }
  public String getResultSubmittedBy() { return resultSubmittedBy; }
  public void setResultSubmittedBy(String v) { resultSubmittedBy = v; }
  public String getResultReviewedBy() { return resultReviewedBy; }
  public void setResultReviewedBy(String v) { resultReviewedBy = v; }
  public String getResultReviewNote() { return resultReviewNote; }
  public void setResultReviewNote(String v) { resultReviewNote = v; }
  public Instant getCreatedAt() { return createdAt; }
}

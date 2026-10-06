package com.pm.careservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity @Table(name = "appointments")
public class Appointment {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID doctorId;
  @Column(nullable = false) private UUID slotId;
  @Column(nullable = false) private String doctorName;
  @Column(nullable = false) private String patientEmail;
  @Column(nullable = false) private LocalDate date;
  @Column(nullable = false) private LocalTime startTime;
  @Column(nullable = false) private LocalTime endTime;
  @Column(nullable = false) private String status = "CONFIRMED";
  private String notes;
  @Column(nullable = false) private Instant createdAt;
  @PrePersist void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
  public UUID getId() { return id; }
  public UUID getDoctorId() { return doctorId; }
  public void setDoctorId(UUID v) { doctorId = v; }
  public UUID getSlotId() { return slotId; }
  public void setSlotId(UUID v) { slotId = v; }
  public String getDoctorName() { return doctorName; }
  public void setDoctorName(String v) { doctorName = v; }
  public String getPatientEmail() { return patientEmail; }
  public void setPatientEmail(String v) { patientEmail = v; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate v) { date = v; }
  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime v) { startTime = v; }
  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime v) { endTime = v; }
  public String getStatus() { return status; }
  public void setStatus(String v) { status = v; }
  public String getNotes() { return notes; }
  public void setNotes(String v) { notes = v; }
  public Instant getCreatedAt() { return createdAt; }
}

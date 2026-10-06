package com.pm.careservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity @Table(name = "doctor_availability")
public class DoctorAvailability {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private UUID doctorId;
  @Column(nullable = false) private LocalDate date;
  @Column(nullable = false) private LocalTime startTime;
  @Column(nullable = false) private LocalTime endTime;
  @Column(nullable = false) private boolean available = true;
  public UUID getId() { return id; }
  public UUID getDoctorId() { return doctorId; }
  public void setDoctorId(UUID v) { doctorId = v; }
  public LocalDate getDate() { return date; }
  public void setDate(LocalDate v) { date = v; }
  public LocalTime getStartTime() { return startTime; }
  public void setStartTime(LocalTime v) { startTime = v; }
  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime v) { endTime = v; }
  public boolean isAvailable() { return available; }
  public void setAvailable(boolean v) { available = v; }
}

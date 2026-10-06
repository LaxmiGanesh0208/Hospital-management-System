package com.pm.careservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "doctors")
public class Doctor {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false) private String name;
  @Column(nullable = false) private String specialization;
  private String qualification;
  @Column(nullable = false) private Integer yearsExperience;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal consultationFee;
  private String avatarUrl;
  @Column(length = 2000) private String bio;
  @Column(nullable = false) private boolean active = true;
  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String v) { name = v; }
  public String getSpecialization() { return specialization; }
  public void setSpecialization(String v) { specialization = v; }
  public String getQualification() { return qualification; }
  public void setQualification(String v) { qualification = v; }
  public Integer getYearsExperience() { return yearsExperience; }
  public void setYearsExperience(Integer v) { yearsExperience = v; }
  public BigDecimal getConsultationFee() { return consultationFee; }
  public void setConsultationFee(BigDecimal v) { consultationFee = v; }
  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String v) { avatarUrl = v; }
  public String getBio() { return bio; }
  public void setBio(String v) { bio = v; }
  public boolean isActive() { return active; }
  public void setActive(boolean v) { active = v; }
}

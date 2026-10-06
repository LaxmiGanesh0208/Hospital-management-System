package com.pm.careservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity @Table(name = "lab_tests")
public class LabTest {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @Column(nullable = false, unique = true) private String code;
  @Column(nullable = false) private String name;
  @Column(nullable = false) private String category;
  @Column(length = 2000) private String description;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
  @Column(nullable = false) private boolean fastingRequired;
  @Column(nullable = false) private boolean active = true;
  private String approvalStatus = "APPROVED";
  @com.fasterxml.jackson.annotation.JsonIgnore private String submittedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore private String reviewedBy;
  @com.fasterxml.jackson.annotation.JsonIgnore @Column(length = 1000) private String reviewNote;
  public UUID getId() { return id; }
  public String getCode() { return code; }
  public void setCode(String v) { code = v; }
  public String getName() { return name; }
  public void setName(String v) { name = v; }
  public String getCategory() { return category; }
  public void setCategory(String v) { category = v; }
  public String getDescription() { return description; }
  public void setDescription(String v) { description = v; }
  public BigDecimal getPrice() { return price; }
  public void setPrice(BigDecimal v) { price = v; }
  public boolean isFastingRequired() { return fastingRequired; }
  public void setFastingRequired(boolean v) { fastingRequired = v; }
  public boolean isActive() { return active; }
  public void setActive(boolean v) { active = v; }
  public String getApprovalStatus() { return approvalStatus; }
  public void setApprovalStatus(String v) { approvalStatus = v; }
  public String getSubmittedBy() { return submittedBy; }
  public void setSubmittedBy(String v) { submittedBy = v; }
  public String getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(String v) { reviewedBy = v; }
  public String getReviewNote() { return reviewNote; }
  public void setReviewNote(String v) { reviewNote = v; }
}

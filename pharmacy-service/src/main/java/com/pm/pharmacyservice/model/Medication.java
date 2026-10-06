package com.pm.pharmacyservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Integer reorderThreshold;

    @Column(nullable = false)
    private BigDecimal price;
    private String category;
    @Column(length = 2000)
    private String description;
    @Column(nullable = false)
    private boolean requiresPrescription;
    @Column
    private String approvalStatus = "APPROVED";
    @com.fasterxml.jackson.annotation.JsonIgnore private String submittedBy;
    @com.fasterxml.jackson.annotation.JsonIgnore private String reviewedBy;
    @com.fasterxml.jackson.annotation.JsonIgnore @Column(length = 1000) private String reviewNote;

    public Medication() {}

    public Medication(UUID id, String name, String code, Integer stockQuantity, Integer reorderThreshold, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.stockQuantity = stockQuantity;
        this.reorderThreshold = reorderThreshold;
        this.price = price;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public Integer getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public void setRequiresPrescription(boolean requiresPrescription) { this.requiresPrescription = requiresPrescription; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String value) { approvalStatus = value; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String value) { submittedBy = value; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String value) { reviewedBy = value; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String value) { reviewNote = value; }
}

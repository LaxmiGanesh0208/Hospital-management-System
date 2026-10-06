package com.pm.pharmacyservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID medicationId;

    @Column(nullable = false)
    private String medicationName;

    @Column(nullable = false)
    private Integer quantity;

    private String dosage;

    @Column(nullable = false)
    private String status; // PENDING, DISPENSED, CANCELLED

    private LocalDateTime createdAt;

    public Prescription() {}

    public Prescription(UUID id, UUID patientId, UUID medicationId, String medicationName, Integer quantity, String dosage, String status, LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.quantity = quantity;
        this.dosage = dosage;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getMedicationId() { return medicationId; }
    public void setMedicationId(UUID medicationId) { this.medicationId = medicationId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.pm.pharmacyservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class PrescriptionRequestDTO {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Medication ID is required")
    private UUID medicationId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String dosage;

    public PrescriptionRequestDTO() {}

    public PrescriptionRequestDTO(UUID patientId, UUID medicationId, Integer quantity, String dosage) {
        this.patientId = patientId;
        this.medicationId = medicationId;
        this.quantity = quantity;
        this.dosage = dosage;
    }

    public UUID getPatientId() { return patientId; }
    public void setPatientId(UUID patientId) { this.patientId = patientId; }

    public UUID getMedicationId() { return medicationId; }
    public void setMedicationId(UUID medicationId) { this.medicationId = medicationId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
}

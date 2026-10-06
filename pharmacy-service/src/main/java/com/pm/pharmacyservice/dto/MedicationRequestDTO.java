package com.pm.pharmacyservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MedicationRequestDTO(@NotBlank String name, @NotBlank String code,
    @NotNull @Min(0) Integer stockQuantity, @NotNull @Min(0) Integer reorderThreshold,
    @NotNull @DecimalMin("0.00") BigDecimal price, @NotBlank String category,
    String description, boolean requiresPrescription) {}

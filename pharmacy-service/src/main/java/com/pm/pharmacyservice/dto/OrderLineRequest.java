package com.pm.pharmacyservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OrderLineRequest(@NotNull UUID medicationId, @NotNull @Positive Integer quantity,
    UUID prescriptionId) {}

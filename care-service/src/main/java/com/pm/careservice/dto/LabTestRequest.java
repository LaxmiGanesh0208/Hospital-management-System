package com.pm.careservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LabTestRequest(@NotBlank String code, @NotBlank String name,
    @NotBlank String category, String description,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    boolean fastingRequired, Boolean active) {}

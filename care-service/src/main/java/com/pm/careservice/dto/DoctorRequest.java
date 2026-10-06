package com.pm.careservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record DoctorRequest(@NotBlank String name, @NotBlank String specialization,
    String qualification, @NotNull @PositiveOrZero Integer yearsExperience,
    @NotNull @DecimalMin("0.00") BigDecimal consultationFee,
    String avatarUrl, String bio, Boolean active) {}

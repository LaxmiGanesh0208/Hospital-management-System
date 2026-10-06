package com.pm.careservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public record LabResultRequest(@NotBlank @Size(max = 4000) String resultSummary,
    @NotNull @Pattern(regexp = "COMPLETED|SAMPLE_COLLECTED") String status) {}

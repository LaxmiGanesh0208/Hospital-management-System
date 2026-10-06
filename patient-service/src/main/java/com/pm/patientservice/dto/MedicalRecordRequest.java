package com.pm.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalRecordRequest(@NotBlank @Email String patientEmail,
    @NotBlank @Size(max = 40) String recordType, @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 4000) String details, @Size(max = 100) String provider) {}

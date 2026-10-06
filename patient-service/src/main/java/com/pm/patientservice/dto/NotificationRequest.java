package com.pm.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NotificationRequest(@NotBlank @Email String patientEmail,
    @NotBlank @Size(max = 40) String type, @NotBlank @Size(max = 160) String title,
    @NotBlank @Size(max = 1000) String message) {}

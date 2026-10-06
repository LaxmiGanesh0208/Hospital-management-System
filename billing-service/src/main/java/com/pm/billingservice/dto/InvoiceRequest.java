package com.pm.billingservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record InvoiceRequest(@NotBlank @Email String patientEmail,
    @NotBlank @Size(max = 40) String referenceType,
    @NotBlank @Size(max = 120) String referenceId,
    @NotBlank @Size(max = 300) String description,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    String currency) {}

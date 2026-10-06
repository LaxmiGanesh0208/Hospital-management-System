package com.pm.careservice.dto;

import java.math.BigDecimal;

public record InvoiceCommand(String patientEmail, String referenceType, String referenceId,
    String description, BigDecimal amount, String currency) {}

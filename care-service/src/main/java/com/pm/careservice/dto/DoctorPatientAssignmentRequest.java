package com.pm.careservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record DoctorPatientAssignmentRequest(@NotNull UUID doctorId,
    @Email @Size(max = 255) String patientEmail, @Size(max = 1000) String note) {}

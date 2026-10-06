package com.pm.careservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AppointmentRequest(@NotNull UUID slotId, @Size(max = 1000) String notes) {}

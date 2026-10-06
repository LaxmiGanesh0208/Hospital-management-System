package com.pm.careservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityRequest(@NotNull @FutureOrPresent LocalDate date,
    @NotNull LocalTime startTime, @NotNull LocalTime endTime) {}

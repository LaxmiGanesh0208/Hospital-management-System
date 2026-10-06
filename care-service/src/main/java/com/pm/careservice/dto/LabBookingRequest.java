package com.pm.careservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record LabBookingRequest(@NotNull UUID testId,
    @NotNull @FutureOrPresent LocalDate date, @NotNull LocalTime time,
    @NotBlank String collectionMethod) {}

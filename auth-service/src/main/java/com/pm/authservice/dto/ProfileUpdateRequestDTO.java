package com.pm.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequestDTO(@NotBlank @Size(max = 100) String fullName,
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 ()-]{7,19}$") String mobile,
    @NotBlank @Size(max = 255) String address,
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 ()-]{7,19}$") String emergencyContact) {}

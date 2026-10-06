package com.pm.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequestDTO(
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 ()-]{7,19}$", message = "Enter a valid mobile number") String mobile,
    @NotBlank @Email String email,
    @NotNull @Past LocalDate dateOfBirth,
    @NotBlank @Size(max = 30) String gender,
    @NotBlank @Size(max = 255) String address,
    @NotBlank @Pattern(regexp = "^[+0-9][0-9 ()-]{7,19}$", message = "Enter a valid emergency contact") String emergencyContact,
    @NotBlank @Size(min = 8, max = 72) String password) {}

package com.pm.apigateway.dto;

import java.util.UUID;

public record AuthIdentityDTO(UUID userId, String email, String role, UUID doctorId) {}

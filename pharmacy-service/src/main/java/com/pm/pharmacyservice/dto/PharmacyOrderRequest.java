package com.pm.pharmacyservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PharmacyOrderRequest(@NotEmpty List<@Valid OrderLineRequest> items,
    @NotBlank @Size(max = 500) String deliveryAddress) {}

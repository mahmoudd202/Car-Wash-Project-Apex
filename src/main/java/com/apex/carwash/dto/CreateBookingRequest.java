package com.apex.carwash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateBookingRequest(
        @NotNull
        @Positive
        Long branchId,

        @NotNull
        @Positive
        Long serviceId,

        @NotBlank
        @Size(max = 100)
        String customerAlias,

        @NotNull
        OffsetDateTime startTime
) {
}

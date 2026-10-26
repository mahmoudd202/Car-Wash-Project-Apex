package com.apex.carwash.dto;

import com.apex.carwash.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull
        BookingStatus status
) {
}

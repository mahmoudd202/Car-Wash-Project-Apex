package com.apex.carwash.dto;

import com.apex.carwash.entity.Booking;
import com.apex.carwash.entity.BookingStatus;
import java.time.OffsetDateTime;

public record BookingResponse(
        Long id,
        Long branchId,
        Long serviceId,
        String customerAlias,
        OffsetDateTime startTime,
        BookingStatus status,
        OffsetDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBranch().getId(),
                booking.getService().getId(),
                booking.getCustomerAlias(),
                booking.getStartTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
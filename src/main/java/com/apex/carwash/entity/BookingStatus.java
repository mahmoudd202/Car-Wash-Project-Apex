package com.apex.carwash.entity;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED;

    public boolean canTransitionTo(BookingStatus next) {
        if (this == PENDING) {
            return next == CONFIRMED || next == CANCELLED;
        }
        if (this == CONFIRMED) {
            return next == COMPLETED || next == CANCELLED;
        }
        return false;
    }
}

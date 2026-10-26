package com.apex.carwash;

import com.apex.carwash.entity.BookingStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookingStatusTest {
    @Test
    void pendingCanBeConfirmedOrCancelled() {
        assertTrue(BookingStatus.PENDING.canTransitionTo(BookingStatus.CONFIRMED));
        assertTrue(BookingStatus.PENDING.canTransitionTo(BookingStatus.CANCELLED));
        assertFalse(BookingStatus.PENDING.canTransitionTo(BookingStatus.COMPLETED));
    }

    @Test
    void confirmedCanBeCompletedOrCancelled() {
        assertTrue(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.COMPLETED));
        assertTrue(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.CANCELLED));
        assertFalse(BookingStatus.CONFIRMED.canTransitionTo(BookingStatus.PENDING));
    }

    @Test
    void terminalStatusesCannotChange() {
        for (BookingStatus next : BookingStatus.values()) {
            assertFalse(BookingStatus.COMPLETED.canTransitionTo(next));
            assertFalse(BookingStatus.CANCELLED.canTransitionTo(next));
        }
    }

    @Test
    void repeatingAStatusIsNotAllowed() {
        for (BookingStatus current : BookingStatus.values()) {
            assertFalse(current.canTransitionTo(current));
        }
    }
}

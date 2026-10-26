package com.apex.carwash.service;

import com.apex.carwash.dto.BookingResponse;
import com.apex.carwash.dto.CreateBookingRequest;
import com.apex.carwash.entity.*;
import com.apex.carwash.exception.ConflictException;
import com.apex.carwash.exception.NotFoundException;
import com.apex.carwash.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookings;
    private final BranchRepository branches;
    private final WashingServiceRepository services;

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {

        Branch branch = branches.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found."));

        WashingService service = services.findById(request.serviceId())
                .orElseThrow(() -> new NotFoundException("Service not found."));

        Booking booking = new Booking(
                branch,
                service,
                request.customerAlias().strip(),
                request.startTime()
        );

        return BookingResponse.from(bookings.saveAndFlush(booking));
    }

    public BookingResponse get(Long id) {

        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found."));

        return BookingResponse.from(booking);
    }

    public List<BookingResponse> list(Long branchId, BookingStatus status, LocalDate date) {

        OffsetDateTime start = null;
        OffsetDateTime end = null;

        if (date != null) {
            start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
            end = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        }

        List<BookingResponse> result = new ArrayList<>();

        for (Booking booking : bookings.findFiltered(
                branchId, status, date != null, start, end)) {
            result.add(BookingResponse.from(booking));
        }

        return result;
    }

    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatus next) {

        Booking booking = bookings.findForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Booking not found."));

        if (!booking.getStatus().canTransitionTo(next)) {
            throw new ConflictException("Invalid booking status transition.");
        }

        booking.setStatus(next);
        bookings.flush();

        return BookingResponse.from(booking);
    }
}
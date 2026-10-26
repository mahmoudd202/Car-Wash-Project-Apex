package com.apex.carwash.controller;

import com.apex.carwash.dto.BookingResponse;
import com.apex.carwash.dto.CreateBookingRequest;
import com.apex.carwash.dto.UpdateStatusRequest;
import com.apex.carwash.entity.BookingStatus;
import com.apex.carwash.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@SecurityRequirement(name = "apiKey")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookings;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody CreateBookingRequest request) {
        BookingResponse created = bookings.create(request);
        return ResponseEntity
                .created(URI.create("/api/bookings/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable @Positive Long id) {
        return bookings.get(id);
    }

    @GetMapping
    public List<BookingResponse> list(
            @RequestParam(required = false) @Positive Long branchId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return bookings.list(branchId, status, date);
    }

    @PatchMapping("/{id}/status")
    public BookingResponse updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        return bookings.updateStatus(id, request.status());
    }
}

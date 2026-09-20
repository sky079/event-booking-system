package com.sumit.eventbooking.api;

import com.sumit.eventbooking.common.PageResponse;
import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.model.booking.BookingRequest;
import com.sumit.eventbooking.model.booking.BookingResponse;
import com.sumit.eventbooking.service.BookingService;
import com.sumit.eventbooking.service.impl.BookingQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingQueryService queries;

    @PostMapping("/api/events/{eventId}/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> book(
            @PathVariable Long eventId,
            @Valid @RequestBody BookingRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Long userId) {
        Booking booking = bookingService.book(userId, eventId, req.quantity(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(booking));
    }

    @GetMapping("/api/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<BookingResponse> mine(@AuthenticationPrincipal Long userId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(queries.mine(userId, page, size).map(BookingResponse::from));
    }

    @GetMapping("/api/bookings/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse get(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return BookingResponse.from(queries.getMine(id, userId));
    }
}
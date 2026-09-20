package com.sumit.eventbooking.api;

import com.sumit.eventbooking.common.PageResponse;
import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.model.booking.BookingRequest;
import com.sumit.eventbooking.model.booking.BookingResponse;
import com.sumit.eventbooking.service.BookingService;
import com.sumit.eventbooking.service.impl.BookingQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Ticket booking APIs")
public class BookingController {

    private final BookingService bookingService;
    private final BookingQueryService queries;

    @Operation(
            summary = "Book tickets for an event",
            description = "Customer books between 1 and 10 tickets."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Not enough tickets available")
    })
    @PostMapping("/api/events/{eventId}/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> book(
            @PathVariable Long eventId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BookingRequest.class),
                            examples = @ExampleObject(
                                    name = "Book Tickets",
                                    value = """
                                            {
                                              "quantity": 2
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody BookingRequest req,

            @RequestHeader(
                    value = "Idempotency-Key",
                    required = false
            ) String idempotencyKey,

            @AuthenticationPrincipal Long userId) {

        Booking booking = bookingService.book(
                userId,
                eventId,
                req.quantity(),
                idempotencyKey
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BookingResponse.from(booking));
    }

    @Operation(summary = "Get current customer's bookings")
    @GetMapping("/api/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<BookingResponse> mine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return PageResponse.from(
                queries.mine(userId, page, size)
                        .map(BookingResponse::from)
        );
    }

    @Operation(summary = "Get a booking by ID")
    @GetMapping("/api/bookings/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public BookingResponse get(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        return BookingResponse.from(
                queries.getMine(id, userId)
        );
    }
}
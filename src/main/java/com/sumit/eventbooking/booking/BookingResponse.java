package com.sumit.eventbooking.booking;

import java.time.Instant;

public record BookingResponse(Long id, Long eventId, Long customerId, int quantity,
                              BookingStatus status, Instant createdAt) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getEventId(), b.getCustomerId(),
                b.getQuantity(), b.getStatus(), b.getCreatedAt());
    }
}
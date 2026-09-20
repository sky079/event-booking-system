package com.sumit.eventbooking.model.booking;

import com.sumit.eventbooking.entity.booking.Booking;

import java.time.Instant;

public record BookingResponse(Long id, Long eventId, Long customerId, int quantity,
                              BookingStatus status, Instant createdAt) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getEventId(), b.getCustomerId(),
                b.getQuantity(), b.getStatus(), b.getCreatedAt());
    }
}
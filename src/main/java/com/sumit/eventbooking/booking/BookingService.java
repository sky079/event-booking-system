package com.sumit.eventbooking.booking;

public interface BookingService {
    Booking book(Long customerId, Long eventId, int quantity, String idempotencyKey);
}
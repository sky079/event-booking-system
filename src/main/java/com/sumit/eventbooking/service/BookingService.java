package com.sumit.eventbooking.service;

import com.sumit.eventbooking.entity.booking.Booking;

public interface BookingService {
    Booking book(Long customerId, Long eventId, int quantity, String idempotencyKey);
}
package com.sumit.eventbooking.service;

import com.sumit.eventbooking.entity.booking.Booking;
import org.springframework.stereotype.Component;


public interface BookingService {
    Booking book(Long customerId, Long eventId, int quantity, String idempotencyKey);
}
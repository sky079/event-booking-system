package com.sumit.eventbooking.service.impl;

import com.sumit.eventbooking.common.ApiException;
import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.model.user.User;
import com.sumit.eventbooking.repository.BookingRepository;
import com.sumit.eventbooking.repository.EventRepository;
import com.sumit.eventbooking.model.event.EventStatus;
import com.sumit.eventbooking.repository.UserRepository;
import com.sumit.eventbooking.service.BookingService;
import com.sumit.eventbooking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * v1: the straightforward implementation. Read the event, check the stock in Java, write the new stock back.
 * Correct for one user at a time. Under concurrency it is NOT safe: two requests can both read the same
 * stock, both pass the check, and both write (lost update -> overselling).
 */
@Service
@ConditionalOnProperty(name = "app.booking.mode", havingValue = "v1", matchIfMissing = true)
@RequiredArgsConstructor
public class BookingServiceV1 implements BookingService {

    private final EventRepository events;
    private final BookingRepository bookings;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Booking book(Long customerId, Long eventId, int quantity, String idempotencyKey) {
        Event event = events.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ApiException(HttpStatus.CONFLICT, "Event is not open for booking");
        }
        if (event.getStartTime().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "Event has already started");
        }
        if (event.getAvailableTickets() < quantity) {
            throw new ApiException(HttpStatus.CONFLICT, "Not enough tickets available");
        }

        event.setAvailableTickets(event.getAvailableTickets() - quantity);   // read-modify-write, not atomic
        events.save(event);

        Booking booking = bookings.save(Booking.confirmed(customerId, eventId, quantity, null)); // key ignored in v1
        User customer = userRepository.findById(booking.getCustomerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found: " + booking.getCustomerId()));
        // Step 6: the confirmation email job is enqueued right here, inside the transaction (the naive way).
        emailService.sendEmail(customer.getEmail(),"Booking Test", "Testing Resend");
        return booking;
    }
}
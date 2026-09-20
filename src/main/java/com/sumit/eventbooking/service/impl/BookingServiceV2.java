package com.sumit.eventbooking.service.impl;

import com.sumit.eventbooking.common.ApiException;
import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.jobs.BookingEmailJob;
import com.sumit.eventbooking.model.event.EventStatus;
import com.sumit.eventbooking.repository.BookingRepository;
import com.sumit.eventbooking.repository.EventRepository;
import com.sumit.eventbooking.service.BookingService;
import com.sumit.eventbooking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@ConditionalOnProperty(
        name = "app.booking.mode",
        havingValue = "v2"
)
@RequiredArgsConstructor
public class BookingServiceV2 implements BookingService {

    private final EventRepository events;
    private final BookingRepository bookings;
    private final BookingEmailJob job;
    private final JobScheduler jobScheduler;

    @Override
    @Transactional
    public Booking book(
            Long customerId,
            Long eventId,
            int quantity,
            String idempotencyKey
    ) {

        Event event = events.findById(eventId)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Event not found"
                        )
                );

        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Event is not open for booking"
            );
        }

        if (event.getStartTime().isBefore(Instant.now())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Event has already started"
            );
        }

        if (quantity <= 0) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than 0"
            );
        }

        // Atomic inventory update
        int updated = events.decreaseTickets(eventId, quantity);

        if (updated == 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Not enough tickets available"
            );
        }

        Booking booking = bookings.save(
                Booking.confirmed(
                        customerId,
                        eventId,
                        quantity,
                        idempotencyKey
                )
        );

        jobScheduler.enqueue(
                () -> job.sendBookingConfirmation(booking.getId())
        );

        return booking;
    }
}
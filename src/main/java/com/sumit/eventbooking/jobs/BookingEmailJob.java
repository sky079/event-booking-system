package com.sumit.eventbooking.jobs;

import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.model.user.User;
import com.sumit.eventbooking.repository.BookingRepository;
import com.sumit.eventbooking.repository.EventRepository;
import com.sumit.eventbooking.repository.UserRepository;
import com.sumit.eventbooking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingEmailJob {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public void sendBookingConfirmation(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found: " + bookingId));

        Event event = eventRepository.findById(booking.getEventId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found: " + booking.getEventId()));

        User customer = userRepository.findById(booking.getCustomerId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Customer not found: " + booking.getCustomerId()));

        emailService.sendEmail(
                customer.getEmail(),
                event.getName(),
                booking.getId().toString()
        );
    }
}

package com.sumit.eventbooking.jobs;

import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.model.booking.BookingStatus;
import com.sumit.eventbooking.model.user.User;
import com.sumit.eventbooking.repository.BookingRepository;
import com.sumit.eventbooking.repository.EventRepository;
import com.sumit.eventbooking.repository.UserRepository;
import com.sumit.eventbooking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventUpdateEmailJob {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void sendEventUpdate(Long eventId, String changes) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Event not found: " + eventId));

        List<Long> customerIds =
                bookingRepository.findDistinctCustomerIds(
                        eventId,
                        BookingStatus.CONFIRMED
                );

        for (Long customerId : customerIds) {

            User customer = userRepository.findById(customerId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Customer not found: " + customerId));

            emailService.sendEventUpdateEmail(
                    customer.getEmail(),
                    event.getName(),
                    changes
            );
        }
    }
}
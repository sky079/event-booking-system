package com.sumit.eventbooking.service.impl;

import com.sumit.eventbooking.entity.booking.Booking;
import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.model.event.EventStats;
import com.sumit.eventbooking.model.event.EventUpdateRequest;
import com.sumit.eventbooking.model.event.UpdateResult;
import com.sumit.eventbooking.model.event.EventCreateRequest;
import com.sumit.eventbooking.model.event.EventStatus;
import com.sumit.eventbooking.repository.BookingRepository;
import com.sumit.eventbooking.model.booking.BookingStatus;
import com.sumit.eventbooking.common.ApiException;
import com.sumit.eventbooking.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sumit.eventbooking.jobs.EventUpdateEmailJob;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository events;
    private final BookingRepository bookings;
    private final JobScheduler jobScheduler;
    private final EventUpdateEmailJob eventUpdateEmailJob;

    @Transactional
    public Event create(Long organizerId, EventCreateRequest r) {
        validateTimes(r.startTime(), r.endTime());
        Event e = new Event();
        e.setOrganizerId(organizerId);
        e.setName(r.name().trim());
        e.setDescription(trim(r.description()));
        e.setLocation(trim(r.location()));
        e.setStartTime(r.startTime());
        e.setEndTime(r.endTime());
        e.setTotalTickets(r.totalTickets());
        e.setAvailableTickets(r.totalTickets());
        return events.save(e);
    }

    @Transactional(readOnly = true)
    public Event get(Long id) {
        return events.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    @Transactional(readOnly = true)
    public Page<Event> listUpcoming(int page, int size) {
        return events.findByStatusAndStartTimeAfter(EventStatus.ACTIVE, Instant.now(), pageable(page, size, "startTime"));
    }

    @Transactional(readOnly = true)
    public Page<Event> listMine(Long organizerId, int page, int size) {
        return events.findByOrganizerId(organizerId, pageable(page, size, "startTime"));
    }

    @Transactional
    public UpdateResult update(Long id, Long organizerId, EventUpdateRequest r) {
        Event e = getOwned(id, organizerId);
        if (e.getStatus() == EventStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Event is cancelled and cannot be updated");
        }
        validateTimes(r.startTime(), r.endTime());

        String name = r.name().trim(), description = trim(r.description()), location = trim(r.location());
        List<String> changes = new ArrayList<>();
        if (!Objects.equals(e.getName(), name))
            changes.add("Name: " + e.getName() + " -> " + name);
        if (!Objects.equals(e.getLocation(), location))
            changes.add("Location: " + e.getLocation() + " -> " + location);
        if (!Objects.equals(e.getStartTime(), r.startTime()))
            changes.add("Start time: " + e.getStartTime() + " -> " + r.startTime());
        if (!Objects.equals(e.getEndTime(), r.endTime()))
            changes.add("End time: " + e.getEndTime() + " -> " + r.endTime());
        if (!Objects.equals(e.getDescription(), description))
            changes.add("Description updated");

        e.setName(name);
        e.setDescription(description);
        e.setLocation(location);
        e.setStartTime(r.startTime());
//        e.setEndTime(r.endTime());
//        events.save(e);      // @DynamicUpdate: only the changed columns are written
//        return new UpdateResult(e, String.join("; ", changes));
        e.setEndTime(r.endTime());
        events.save(e);

        String changeSummary = String.join("; ", changes);

        if (!changes.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            jobScheduler.enqueue(
                                    () -> eventUpdateEmailJob.sendEventUpdate(
                                            e.getId(),
                                            changeSummary
                                    )
                            );
                        }
                    }
            );
        }

        return new UpdateResult(e, changeSummary);
    }

    /** Soft delete. Returns true if the event was newly cancelled (false if it already was). */
    @Transactional
    public boolean cancel(Long id, Long organizerId) {
        Event e = getOwned(id, organizerId);
        if (e.getStatus() == EventStatus.CANCELLED) return false;
        e.setStatus(EventStatus.CANCELLED);
        events.save(e);
        return true;
    }

    @Transactional(readOnly = true)
    public Page<Booking> bookingsFor(Long id, Long organizerId, int page, int size) {
        getOwned(id, organizerId);
        return bookings.findByEventId(id, pageable(page, size, "id"));
    }

    @Transactional(readOnly = true)
    public EventStats stats(Long id, Long organizerId) {
        Event e = getOwned(id, organizerId);
        long confirmed = bookings.sumConfirmedTickets(id, BookingStatus.CONFIRMED);
        boolean oversold = confirmed > e.getTotalTickets();
        boolean consistent = (e.getTotalTickets() - e.getAvailableTickets()) == confirmed;
        return new EventStats(id, e.getTotalTickets(), e.getAvailableTickets(), confirmed, oversold, consistent);
    }

    // ---- helpers ----

    /** Ownership check: role alone is not enough, organizer A must not touch organizer B's event. */
    private Event getOwned(Long id, Long organizerId) {
        Event e = get(id);
        if (!e.getOrganizerId().equals(organizerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this event");
        }
        return e;
    }

    private static void validateTimes(Instant start, Instant end) {
        if (end != null && !end.isAfter(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endTime must be after startTime");
        }
    }

    private static Pageable pageable(int page, int size, String sortBy) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(sortBy).ascending());
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
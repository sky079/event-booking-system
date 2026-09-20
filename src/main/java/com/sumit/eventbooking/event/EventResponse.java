package com.sumit.eventbooking.event;

import java.time.Instant;

public record EventResponse(Long id, Long organizerId, String name, String description, String location,
                            Instant startTime, Instant endTime, int totalTickets, int availableTickets,
                            EventStatus status, Instant createdAt, Instant updatedAt) {
    public static EventResponse from(Event e) {
        return new EventResponse(e.getId(), e.getOrganizerId(), e.getName(), e.getDescription(), e.getLocation(),
                e.getStartTime(), e.getEndTime(), e.getTotalTickets(), e.getAvailableTickets(),
                e.getStatus(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
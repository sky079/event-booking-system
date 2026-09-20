package com.sumit.eventbooking.model.event;

public record EventStats(Long eventId, int totalTickets, int availableTickets,
                         long confirmedTickets, boolean oversold, boolean consistent) {}
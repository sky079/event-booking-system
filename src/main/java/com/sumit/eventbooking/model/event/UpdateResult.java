package com.sumit.eventbooking.model.event;

import com.sumit.eventbooking.entity.event.Event;

public record UpdateResult(Event event, String changeSummary) {}
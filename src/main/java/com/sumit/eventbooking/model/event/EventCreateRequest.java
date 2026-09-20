package com.sumit.eventbooking.model.event;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record EventCreateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @Size(max = 200) String location,
        @NotNull @Future Instant startTime,
        Instant endTime,
        @Min(1) @Max(10_000_000) int totalTickets) {}
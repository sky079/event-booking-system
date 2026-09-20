package com.sumit.eventbooking.event;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record EventUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @Size(max = 200) String location,
        @NotNull @Future Instant startTime,
        Instant endTime) {}
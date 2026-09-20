package com.sumit.eventbooking.api;

import com.sumit.eventbooking.common.PageResponse;
import com.sumit.eventbooking.model.booking.BookingResponse;
import com.sumit.eventbooking.model.event.*;
import com.sumit.eventbooking.service.impl.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService service;

    @Operation(
            summary = "Create a new event",
            description = "Organizer creates a new event with ticket capacity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Organizer access required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EventCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "Create Event",
                                    value = """
                                            {
                                              "name": "Java Backend Workshop",
                                              "description": "Spring Boot and REST API workshop",
                                              "location": "Bangalore",
                                              "startTime": "2027-01-15T10:00:00Z",
                                              "endTime": "2027-01-15T13:00:00Z",
                                              "totalTickets": 100
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody EventCreateRequest req,
            @AuthenticationPrincipal Long userId) {

        return ResponseEntity.status(201)
                .body(EventResponse.from(service.create(userId, req)));
    }

    @Operation(summary = "List upcoming events")
    @GetMapping
    public PageResponse<EventResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return PageResponse.from(
                service.listUpcoming(page, size)
                        .map(EventResponse::from)
        );
    }

    @Operation(summary = "List events created by current organizer")
    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER')")
    public PageResponse<EventResponse> mine(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return PageResponse.from(
                service.listMine(userId, page, size)
                        .map(EventResponse::from)
        );
    }

    @Operation(summary = "Get event by ID")
    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        return EventResponse.from(service.get(id));
    }

    @Operation(
            summary = "Update an event",
            description = "Organizer can update event details."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse update(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = EventUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "Update Event",
                                    value = """
                                            {
                                              "name": "Advanced Java Backend Workshop",
                                              "description": "Updated workshop description",
                                              "location": "Bangalore",
                                              "startTime": "2027-01-20T10:00:00Z",
                                              "endTime": "2027-01-20T14:00:00Z"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody EventUpdateRequest req,
            @AuthenticationPrincipal Long userId) {

        UpdateResult result = service.update(id, userId, req);
        return EventResponse.from(result.event());
    }

    @Operation(summary = "Cancel an event")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        service.cancel(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get bookings for an event")
    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasRole('ORGANIZER')")
    public PageResponse<BookingResponse> bookings(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return PageResponse.from(
                service.bookingsFor(id, userId, page, size)
                        .map(BookingResponse::from)
        );
    }

    @Operation(
            summary = "Get event statistics",
            description = "Returns ticket availability, confirmed tickets and consistency information."
    )
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventStats stats(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {

        return service.stats(id, userId);
    }
}
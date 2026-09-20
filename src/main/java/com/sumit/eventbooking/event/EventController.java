package com.sumit.eventbooking.event;

import com.sumit.eventbooking.booking.BookingResponse;
import com.sumit.eventbooking.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventCreateRequest req,
                                                @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(201).body(EventResponse.from(service.create(userId, req)));
    }

    @GetMapping
    public PageResponse<EventResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(service.listUpcoming(page, size).map(EventResponse::from));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('ORGANIZER')")
    public PageResponse<EventResponse> mine(@AuthenticationPrincipal Long userId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(service.listMine(userId, page, size).map(EventResponse::from));
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable Long id) {
        return EventResponse.from(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventResponse update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest req,
                                @AuthenticationPrincipal Long userId) {
        UpdateResult result = service.update(id, userId, req);
        // Step 6: if result.changeSummary() is not empty, enqueue the "event updated" notification job here.
        return EventResponse.from(result.event());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        service.cancel(id, userId);
        // Step 6: if it was newly cancelled, notify booked customers here too.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/bookings")
    @PreAuthorize("hasRole('ORGANIZER')")
    public PageResponse<BookingResponse> bookings(@PathVariable Long id, @AuthenticationPrincipal Long userId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(service.bookingsFor(id, userId, page, size).map(BookingResponse::from));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('ORGANIZER')")
    public EventStats stats(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return service.stats(id, userId);
    }
}
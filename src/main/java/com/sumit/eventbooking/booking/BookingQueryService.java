package com.sumit.eventbooking.booking;

import com.sumit.eventbooking.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private final BookingRepository bookings;

    @Transactional(readOnly = true)
    public Page<Booking> mine(Long customerId, int page, int size) {
        return bookings.findByCustomerId(customerId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id")));
    }

    /** Someone else's booking looks exactly like a missing one (404), so IDs can't be probed. */
    @Transactional(readOnly = true)
    public Booking getMine(Long id, Long customerId) {
        return bookings.findById(id)
                .filter(b -> b.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}
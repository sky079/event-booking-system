package com.sumit.eventbooking.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findByEventId(Long eventId);

    Optional<Booking> findByCustomerIdAndIdempotencyKey(Long customerId, String idempotencyKey);

    // Who to notify when an event changes (each customer once, even with several bookings)
    @Query("select distinct b.customerId from Booking b where b.eventId = :eventId and b.status = :status")
    List<Long> findDistinctCustomerIds(@Param("eventId") Long eventId, @Param("status") BookingStatus status);

    // For the overselling check: tickets actually sold for an event
    @Query("select coalesce(sum(b.quantity), 0) from Booking b where b.eventId = :eventId and b.status = :status")
    long sumConfirmedTickets(@Param("eventId") Long eventId, @Param("status") BookingStatus status);

    Page<Booking> findByEventId(Long eventId, Pageable pageable);
}
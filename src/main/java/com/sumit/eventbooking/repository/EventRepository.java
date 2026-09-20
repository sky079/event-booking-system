package com.sumit.eventbooking.repository;

import com.sumit.eventbooking.entity.event.Event;
import com.sumit.eventbooking.model.event.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByStatusAndStartTimeAfter(EventStatus status, Instant after, Pageable pageable);

    Page<Event> findByOrganizerId(Long organizerId, Pageable pageable);

    // Used in v2 only: atomic reserve. Returns 1 if tickets were taken, 0 if sold out / cancelled / not found.
    @Modifying
    @Query(value = """
        UPDATE events
           SET available_tickets = available_tickets - :qty
         WHERE id = :id
           AND status = 'ACTIVE'
           AND available_tickets >= :qty
        """, nativeQuery = true)
    int reserveTickets(@Param("id") Long id, @Param("qty") int qty);
}
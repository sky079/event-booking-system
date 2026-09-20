package com.sumit.eventbooking.booking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_bookings_event", columnList = "event_id"),
                @Index(name = "idx_bookings_customer", columnList = "customer_id")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookings_customer_idem", columnNames = {"customer_id", "idempotency_key"}))
@Getter @Setter @NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "idempotency_key")      // unused in v1, used in v2
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static Booking confirmed(Long customerId, Long eventId, int quantity, String idempotencyKey) {
        Booking b = new Booking();
        b.customerId = customerId;
        b.eventId = eventId;
        b.quantity = quantity;
        b.idempotencyKey = idempotencyKey;
        return b;
    }
}
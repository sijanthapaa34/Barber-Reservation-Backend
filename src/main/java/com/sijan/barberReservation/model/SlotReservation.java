package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "slot_reservations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"barber_id", "reserved_time"}),
                @UniqueConstraint(columnNames = {"payment_transaction_id"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "barber_id", nullable = false)
    private Long barberId;

    @Column(name = "reserved_time", nullable = false)
    private LocalDateTime reservedTime;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "payment_transaction_id", nullable = false, unique = true)
    private Long paymentTransactionId;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    public enum ReservationStatus {
        ACTIVE, CONSUMED, EXPIRED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (reservedAt == null) reservedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

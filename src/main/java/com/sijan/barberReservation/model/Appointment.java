package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private BarberShop barbershop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToMany
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ServiceOffering> services;

    // Appointment details
    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private Integer totalDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    // Timing
    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private LocalDateTime checkInTime;
    private LocalDateTime completedTime;

    // Payment information
    private LocalDateTime paymentTime;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    private Double paidAmount;
    private String paymentMethod;

    // Notes
    private String customerNotes;
    private String barberNotes;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper methods
    @Transient
    public LocalTime getStartTime() {
        return scheduledTime.toLocalTime();
    }

    @Transient
    public LocalTime getEndTime() {
        return scheduledTime.toLocalTime().plusMinutes(totalDurationMinutes);
    }

    @Transient
    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }

    @Transient
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
}
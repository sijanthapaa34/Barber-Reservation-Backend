//package com.sijan.barberReservation.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class Appointment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // Relationships
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "barber_id", nullable = false)
//    private Barber barber;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "barbershop_id", nullable = false)
//    private Barbershop barbershop;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id", nullable = false)
//    private Customer customer;
//
//    @ManyToMany
//    @JoinTable(
//            name = "appointment_services",
//            joinColumns = @JoinColumn(name = "appointment_id"),
//            inverseJoinColumns = @JoinColumn(name = "service_id")
//    )
//    private List<ServiceOffering> services;
//
//    // Appointment details
//    @Column(nullable = false)
//    private Double totalPrice;
//
//    @Column(nullable = false)
//    private Integer totalDurationMinutes;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
//
//    @Enumerated(EnumType.STRING)
//    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
//
//    // Tracks the final method used for this booking (e.g., ESEWA, CASH)
//    @Enumerated(EnumType.STRING)
//    private PaymentMethod paymentMethod;
//
//    // Timing
//    @Column(nullable = false)
//    private LocalDateTime scheduledTime;
//
//    private LocalDateTime checkInTime;
//    private LocalDateTime completedTime;
//
//    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL)
//    private List<PaymentTransaction> transactions = new ArrayList<>();
//
//    private String cancelledBy;
//
//    // Notes
//    private String customerNotes;
//    private String barberNotes;
//
//    // Timestamps
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
//
//    // Helper methods
//    @Transient
//    public LocalTime getStartTime() {
//        return scheduledTime.toLocalTime();
//    }
//
//    @Transient
//    public LocalTime getEndTime() {
//        return scheduledTime.toLocalTime().plusMinutes(totalDurationMinutes);
//    }
//
//    @Transient
//    public boolean isCompleted() {
//        return status == AppointmentStatus.COMPLETED;
//    }
//    public boolean isCancelled() {
//        return status == AppointmentStatus.CANCELLED;
//    }
//}
package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "appointments",
        uniqueConstraints = {
                // Prevent double booking at DB level
                @UniqueConstraint(
                        name = "uk_barber_scheduled_time",
                        columnNames = {"barber_id", "scheduled_time"}
                )
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @ManyToMany
    @JoinTable(
            name = "appointment_services",
            joinColumns = @JoinColumn(name = "appointment_id"),
            inverseJoinColumns = @JoinColumn(name = "service_offering_id")
    )
    private List<ServiceOffering> services;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }

    public boolean isCancelled() {
        return status == AppointmentStatus.CANCELLED;
    }

    public boolean isScheduled() {
        return status == AppointmentStatus.SCHEDULED;
    }
}
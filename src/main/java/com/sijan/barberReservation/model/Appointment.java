package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Barber barber;

    @OneToOne
    private Customer customer;

    @ManyToOne
    private Services service;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private LocalDateTime checkInTime;

    private LocalDateTime scheduledTime;

    private LocalDateTime createdAt;
}


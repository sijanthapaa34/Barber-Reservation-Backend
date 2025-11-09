package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Appointment appointment;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus action; // e.g., BOOKED, CANCELED, RESCHEDULED, CHECKED_IN

    private String description; // optional extra info

    private LocalDateTime timestamp = LocalDateTime.now();

    private String performedBy; // userId or "SYSTEM"
}

package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Barber barber;

    private LocalDate startDate;
    private LocalDate endDate;

    private String reason;

    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

}


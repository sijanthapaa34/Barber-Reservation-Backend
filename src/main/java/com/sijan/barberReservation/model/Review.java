package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Barber barber;
    @ManyToOne(fetch = FetchType.LAZY)
    private BarberShop barberShop;
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Appointment appointment;
    private int rating;
    private String comment;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

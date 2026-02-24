package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

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
    private Barbershop barberShop;
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;
    private int rating;

    private List<String> images;
    private List<String> comments;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

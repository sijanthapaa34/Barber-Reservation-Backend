package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Barber extends User {

    // Professional information
    @Column(length = 1000)
    private String bio;

    @Column(nullable = false)
    private Integer experienceYears;

    private String profilePicture;

    // Rating
    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    // Availability
    @Column(nullable = false)
    private Boolean available = true;

    // Commission rate (percentage of service price)
    private Double commissionRate = 30.0; // Default 30%

    // Skills
    @ElementCollection
    @CollectionTable(name = "barber_skills", joinColumns = @JoinColumn(name = "barber_id"))
    @Column(name = "skill")
    private List<String> skills;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private BarberShop barbershop;

    @OneToMany(mappedBy = "barber")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "barber")
    private List<Review> reviews;

    @OneToMany(mappedBy = "barber")
    private List<BarberSchedule> schedules;

    @OneToMany(mappedBy = "barber")
    private List<BarberLeave> leaves;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to check if barber is top rated
    @Transient
    public boolean isTopRated() {
        return rating >= 4.5 && reviewCount >= 20;
    }
}
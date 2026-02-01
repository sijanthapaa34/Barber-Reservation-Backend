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
public class Customer extends User {

    // Loyalty program
    @Column(nullable = false)
    private Integer points = 0;

    // Customer preferences
    @Column(length = 500)
    private String preferences;

    // Visit tracking
    private LocalDateTime lastVisited;
    private Integer totalBookings = 0;
    private Integer noShowCount = 0;

    // Customer status
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "customer")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "customer")
    private List<Review> reviews;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Helper method to check if customer is VIP
    @Transient
    public boolean isVip() {
        return points >= 1000 || totalBookings >= 50;
    }
}
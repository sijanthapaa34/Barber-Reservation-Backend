package com.sijan.barberReservation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends User{
    private double points;

    private String preferences;

    private LocalDateTime lastVisited;

    private Integer totalBookings = 0;

    private Integer noShowCount = 0;

    @OneToMany(mappedBy = "customer")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "customer")
    private List<Review> reviews;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
}

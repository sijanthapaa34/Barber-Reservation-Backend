package com.sijan.barberReservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Barber extends User {

    @Column(length = 1000)
    private String bio;
    private Integer experienceYears;
    private String profilePicture;
    private Double rating = 0.0;
    private Boolean available = true;

    @OneToMany(mappedBy = "barber")
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "barber")
    private List<Review> reviews;

    @OneToMany(mappedBy = "barber")
    private List<BarberSchedule> schedules;

    @OneToMany(mappedBy = "barber")
    private List<BarberLeave> leaves;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;
}

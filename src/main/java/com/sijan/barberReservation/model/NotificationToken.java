package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class NotificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID of Customer, Barber, or Admin
    private String token; // The FCM Device Token
    private String userType; // "CUSTOMER", "BARBER", "ADMIN"
}
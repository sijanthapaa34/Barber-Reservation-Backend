package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String phone;
    private String password;
    private Boolean active = true;
    @Enumerated(EnumType.STRING)
    private Roles role;
    private LocalDateTime createdAt;
}

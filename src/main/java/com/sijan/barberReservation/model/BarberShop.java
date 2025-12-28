package com.sijan.barberReservation.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberShop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String city;

    @Column(nullable = false)
    private String phone;

    private String email;

    private boolean active = true;

    @OneToMany(mappedBy = "barberShop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Barber> barbers;
}

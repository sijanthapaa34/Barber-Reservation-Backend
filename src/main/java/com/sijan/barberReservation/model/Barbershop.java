package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Barbershop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic shop information
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String state;
    private String postalCode;
    private String country;

    @DecimalMin(value = "-90.000000", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.000000", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.000000", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.000000", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    // Full address string for display
    private String fullAddress;

    @Email(message = "Invalid email format")
    private String email;

    private String website;

    // Shop status
    private boolean active = true;

    // Ratings
    private Double rating = 0.0;
    private Integer reviewCount = 0;

    @Column(columnDefinition = "text")
    private String operatingHours;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Barber> barbers;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOffering> services;

    @OneToMany(mappedBy = "barbershop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;

    @ManyToOne
    private Admin admin;

    @URL(message = "Profile picture must be a valid URL")
    @Size(max = 2048, message = "URL is too long")
    private String profilePicture;
}
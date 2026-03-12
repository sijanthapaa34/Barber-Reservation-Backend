package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ApplicationType type;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    // Common
    private String name;
    private String email;
    private String phone;
    private String password;

    // Barber Specific
    private Integer experienceYears;
    @Column(length = 2000)
    private String skills;
    private String bio;
    private String city;
    private String profilePictureUrl;
    private Long barbershopId;
    private String barbershopName;


    // Shop Specific
    private String shopName;
    private String ownerName;
    private String address;
    private String state;
    private String postalCode;

    // UPDATED: Use BigDecimal with precision
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    private String website;
    private String operatingHours;
    private String description;
    @ElementCollection
    @CollectionTable(name = "application_shop_images", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "image_url", length = 500)
    private List<String> shopImages = new ArrayList<>();

    // Documents
    @Column(length = 500)
    private String licenseUrl;
    @Column(length = 500)
    private String documentUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String rejectionReason;
}



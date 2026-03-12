package com.sijan.barberReservation.DTO.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationDetailResponse {

    private Long id;
    private String type;
    private String status;

    // --- Common Account Info ---
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

    // --- Barber Specific Fields ---
    private Integer experienceYears;
    private List<String> skills;
    private String bio;
    private String city;
    private String profilePictureUrl;
    private String licenseUrl;
    private String barbershopName;

    // --- Shop Specific Fields ---
    private String shopName;     // ADDED
    private String address;
    private String state;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String website;
    private String operatingHours;
    private String description;
    private String documentUrl;
    private List<String> shopImages;

    // --- Admin Review Info ---
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String rejectionReason;
}
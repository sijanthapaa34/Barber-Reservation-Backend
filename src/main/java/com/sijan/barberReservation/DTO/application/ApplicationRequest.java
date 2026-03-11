package com.sijan.barberReservation.DTO.application;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ApplicationRequest {
    private String type;

    private String name;
    private String email;
    private String phone;
    private String password;

    private Integer experienceYears;
    private List<String> skills;
    private String bio;
    private String city;
    private String profilePictureUrl;
    private String licenseUrl;
    private Long barbershopId;
    private String barbershopName;

    private String shopName;
    private String ownerName;
    private String address;
    private String state;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> shopImages;
    private String website;
    private String operatingHours;
    private String description;
    private String documentUrl;
}
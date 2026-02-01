package com.sijan.barberReservation.DTO.service;

import com.sijan.barberReservation.model.ServiceCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateServiceRequest {

    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;

    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    private Integer durationMinutes;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "999.99", message = "Price cannot exceed 999.99")
    @Digits(integer = 3, fraction = 2, message = "Price must have at most 3 integer digits and 2 decimal places")
    private Double price;

    private ServiceCategory category;
    private Boolean available;

    // Optional fields for update
    private String requirements;
    private String preparationNotes;
    private Boolean requiresConsultation;
    private Integer advanceBookingDays;

    // Pricing options
    private Boolean hasDiscount;
    private Double discountPercentage;
    private Boolean discountActive;

    // Service availability
    private Boolean isSeasonal;
    private String seasonStart;
    private String seasonEnd;

    // Media
    private String serviceImage;
    private List<String> serviceImages;

    // Target audience
    private String targetGender;
    private Integer minAge;
    private Integer maxAge;

    // Complexity level
    private String complexityLevel;

    // Service tags
    private List<String> tags;

    // Barbers who can perform this service
    private List<Long> preferredBarberIds;

    // Additional metadata
    private String metaTitle; // For SEO
    private String metaDescription; // For SEO
    private String metaKeywords; // For SEO
}
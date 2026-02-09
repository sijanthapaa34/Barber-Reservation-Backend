package com.sijan.barberReservation.DTO.service;

import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.model.ServiceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterServiceRequest {

    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "999.99", message = "Price cannot exceed 999.99")
    @Digits(integer = 3, fraction = 2, message = "Price must have at most 3 integer digits and 2 decimal places")
    private Double price;

    @NotNull(message = "Category is required")
    private ServiceCategory category;

    private Boolean available = true;

    // Media
    private String serviceImage; // URL to service image
    private List<String> serviceImages; // Multiple images

    // Target audience
    private String targetGender; // "MALE", "FEMALE", "ALL", "CHILDREN"
//    private Integer minAge = 0;
//    private Integer maxAge = 100;
//
//    // Complexity level
//    private String complexityLevel; // "BASIC", "INTERMEDIATE", "ADVANCED"
//
//    // Service tags for search
//    private List<String> tags;
//
//    // Barbers who can perform this service (if specified)
//    private List<Long> preferredBarberIds;
}

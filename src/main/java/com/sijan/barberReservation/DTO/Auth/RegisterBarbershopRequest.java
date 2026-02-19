package com.sijan.barberReservation.DTO.Auth;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Data
public class RegisterBarbershopRequest {
    @NotBlank(message = "Name is required")
    private String shopName;

    @NotBlank(message = "Admin Name is required")
    private String adminName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String state;
    private String postalCode;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    @Email(message = "Invalid email format")
    private String shopEmail;

    @Email(message = "Invalid email format")
    private String adminEmail;
    private String website;
    private String operatingHours;
    @URL(message = "Profile picture must be a valid URL")
    @Size(max = 2048, message = "URL is too long")
    private String adminProfilePicture;
    @URL(message = "Profile picture must be a valid URL")
    @Size(max = 2048, message = "URL is too long")
    private String barbershopProfilePicture;
}

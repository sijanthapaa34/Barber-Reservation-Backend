package com.sijan.barberReservation.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class RegisterCustomerRequest {
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String preferences;
    @URL(message = "Profile picture must be a valid URL")
    @Size(max = 2048, message = "URL is too long")
    private String profilePicture;

}

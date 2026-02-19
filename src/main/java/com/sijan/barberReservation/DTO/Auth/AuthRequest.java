package com.sijan.barberReservation.DTO.Auth;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    @Email(message = "Invalid email format")
    private String email;
    private String password;
}

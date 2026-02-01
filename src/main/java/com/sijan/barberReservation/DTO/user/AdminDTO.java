package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.model.AdminLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AdminDTO {
    private Long id;
    private String name;
    private String email;
    private AdminLevel adminLevel;
    private Long barbershopId;
    private String barbershopName;
    private String token;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private Boolean requiresPasswordChange;
}

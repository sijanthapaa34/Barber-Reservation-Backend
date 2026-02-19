package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.model.AdminLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class AdminDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private AdminLevel adminLevel;
    private String profilePicture;
    private Long barbershopId;
    private String barbershopName;
}

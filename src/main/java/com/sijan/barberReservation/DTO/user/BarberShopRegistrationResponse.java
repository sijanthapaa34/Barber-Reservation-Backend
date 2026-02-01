package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarberShopRegistrationResponse {
    private BarberShopDTO barbershop;
    private AdminDTO admin;
    private String message;
    private String registrationDate;
}
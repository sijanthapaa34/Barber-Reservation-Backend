package com.sijan.barberReservation.DTO.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {
    private String name;
    private int durationMinutes;
    private Double price;
}

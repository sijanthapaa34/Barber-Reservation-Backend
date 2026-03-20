package com.sijan.barberReservation.DTO.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateRequest {
    private int durationMinutes;
    private Double price;
    private List<String> serviceImages;
}

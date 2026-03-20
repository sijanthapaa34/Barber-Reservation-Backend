package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBarberRequest {
    private String bio;
    private Integer experienceYears;
    private List<String> skills;
    private List<String> workImages;
    private Double commissionRate;
}

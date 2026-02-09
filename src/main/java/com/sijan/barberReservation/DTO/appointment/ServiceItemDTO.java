package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItemDTO {
    private Long serviceId;
    private String name;
    private Double price;
    private Integer durationMinutes;
}

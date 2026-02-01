package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailsDTO {

    private String customerName;
    private String barberName;
    private List<ServiceItemDTO> services;
    private Double totalPrice;

    private LocalDateTime scheduledTime;
    private LocalDateTime checkInTime;
    private LocalDateTime createdAt;
    private String status;
}

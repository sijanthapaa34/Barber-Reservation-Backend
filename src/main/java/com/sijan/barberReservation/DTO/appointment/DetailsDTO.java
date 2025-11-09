package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailsDTO {

    private String customerName;
    private String barberName;
    private String serviceName;
    private Double servicePrice;

    private LocalDateTime scheduledTime;
    private LocalDateTime checkInTime;
    private LocalDateTime createdAt;
    private String status;
}

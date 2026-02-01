package com.sijan.barberReservation.DTO.service;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServiceDTO {
    private String name;
    private String description;
    private Double price;
    private Integer durationMinutes;
    private Long barberShopId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
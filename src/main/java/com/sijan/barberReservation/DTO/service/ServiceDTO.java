package com.sijan.barberReservation.DTO.service;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Boolean available;
    private String category;
    private Integer durationMinutes;
    private String barbershop;
    private Integer barbershopId;
    private String targetGender;
    private List<String> serviceImages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

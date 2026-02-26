package com.sijan.barberReservation.DTO.service;

import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<String> serviceImages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarberDTO {
    private String name;
    private String barberShop;
    private boolean active;
    private String email;
    private String phone;
    private String bio;
    private String profilePictureUrl;
    private Double rating;
    private LocalDateTime createdAt;
    private Integer experienceYears;
    private Boolean available;


}
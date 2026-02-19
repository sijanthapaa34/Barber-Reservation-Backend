package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BarbershopDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String phone;
    private String email;
    private String website;
    private String operatingHours;
    private String profilePicture;
    private Double rating;
    }


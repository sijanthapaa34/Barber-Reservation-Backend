package com.sijan.barberReservation.DTO.user;

import lombok.Data;

@Data
public class RegisterBarberRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String bio;
    private Integer experienceYears;
    private String profilePicture;
}
package com.sijan.barberReservation.DTO.Auth;

import lombok.Data;

@Data
public class RegisterCustomerRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String preferences;
}

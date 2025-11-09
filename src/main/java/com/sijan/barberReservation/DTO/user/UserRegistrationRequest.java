package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
}

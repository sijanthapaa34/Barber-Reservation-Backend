package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrequentCustomerDTO {

    private String name;
    private String email;
    private int totalBookings;
    private Double points;
}

package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrequentCustomerDTO {
    private Long id;
    private String name;
    private String email;
    private int totalBookings;
    private Integer points;
}

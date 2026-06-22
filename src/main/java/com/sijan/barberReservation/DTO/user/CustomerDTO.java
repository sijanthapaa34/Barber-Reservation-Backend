package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.model.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Integer points;
    private Integer totalBookings;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastBookingAt;
    private String profilePicture;
}

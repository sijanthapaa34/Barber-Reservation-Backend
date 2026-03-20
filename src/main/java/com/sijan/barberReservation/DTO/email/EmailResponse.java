package com.sijan.barberReservation.DTO.email;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailResponse {
    private String message;
    private boolean success;
}

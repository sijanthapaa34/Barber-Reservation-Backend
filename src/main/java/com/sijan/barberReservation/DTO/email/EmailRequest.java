package com.sijan.barberReservation.DTO.email;

import lombok.Data;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String body;

    // For specific templates
    private String userName;
    private String shopName;
    private String status; // e.g., "APPROVED", "REJECTED"
}
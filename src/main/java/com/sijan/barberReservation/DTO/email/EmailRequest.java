package com.sijan.barberReservation.DTO.email;

import lombok.Data;

@Data
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private String userName;
    private String shopName;
    private String status;
}
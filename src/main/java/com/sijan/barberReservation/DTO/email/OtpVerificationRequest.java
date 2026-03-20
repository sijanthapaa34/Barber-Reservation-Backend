package com.sijan.barberReservation.DTO.email;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otp;
}

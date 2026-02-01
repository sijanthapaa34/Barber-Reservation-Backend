package com.sijan.barberReservation.DTO.google;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class GoogleLoginRequest {
    private String idToken;
}
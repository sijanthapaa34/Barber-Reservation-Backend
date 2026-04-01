package com.sijan.barberReservation.DTO.notification;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;
}
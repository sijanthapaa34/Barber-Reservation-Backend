package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingStatusRequest {
    private Long chatRoomId;
    private String userType; // CUSTOMER or ADMIN
    private Boolean isTyping;
}

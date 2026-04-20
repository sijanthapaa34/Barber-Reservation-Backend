package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private String senderType; // CUSTOMER or ADMIN
    private String messageText;
}

package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String senderName;
    private String senderType;
    private String messageText;
    private String status;
    private LocalDateTime createdAt;
}
package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadStatusRequest {
    private Long chatRoomId;
    private String userType;
}
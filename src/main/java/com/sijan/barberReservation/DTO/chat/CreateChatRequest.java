package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRequest {
    private Long customerId;
    private String customerName;
    private Long shopAdminId;
    private String shopAdminName;
    private Long shopId;
    private String shopName;
}

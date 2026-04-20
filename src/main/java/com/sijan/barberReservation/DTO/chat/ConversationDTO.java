package com.sijan.barberReservation.DTO.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long shopAdminId;
    private String shopAdminName;
    private Long shopId;
    private String shopName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCountCustomer;
    private Integer unreadCountAdmin;
    private Boolean customerOnline;
    private Boolean adminOnline;
    private Boolean customerTyping;
    private Boolean adminTyping;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

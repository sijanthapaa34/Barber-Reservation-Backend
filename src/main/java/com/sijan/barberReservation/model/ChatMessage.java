package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;
    
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    
    @Column(name = "sender_name", nullable = false)
    private String senderName;
    
    @Column(name = "sender_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SenderType senderType;
    
    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum SenderType {
        CUSTOMER, ADMIN
    }
    
    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
}

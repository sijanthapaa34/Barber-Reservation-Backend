package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(name = "shop_admin_id", nullable = false)
    private Long shopAdminId;
    
    @Column(name = "shop_admin_name", nullable = false)
    private String shopAdminName;
    
    @Column(name = "shop_id", nullable = false)
    private Long shopId;
    
    @Column(name = "shop_name", nullable = false)
    private String shopName;
    
    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;
    
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;
    
    @Column(name = "unread_count_customer", nullable = false)
    private Integer unreadCountCustomer = 0;
    
    @Column(name = "unread_count_admin", nullable = false)
    private Integer unreadCountAdmin = 0;
    
    @Column(name = "customer_online", nullable = false)
    private Boolean customerOnline = false;
    
    @Column(name = "admin_online", nullable = false)
    private Boolean adminOnline = false;
    
    @Column(name = "customer_typing", nullable = false)
    private Boolean customerTyping = false;
    
    @Column(name = "admin_typing", nullable = false)
    private Boolean adminTyping = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastMessageTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.chat.*;
import com.sijan.barberReservation.model.ChatMessage;
import com.sijan.barberReservation.model.ChatRoom;
import com.sijan.barberReservation.repository.ChatMessageRepository;
import com.sijan.barberReservation.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    
    /**
     * Get or create a chat room between customer and shop admin
     */
    @Transactional
    public ConversationDTO getOrCreateChatRoom(CreateChatRequest request) {
        log.info("Getting or creating chat room for customer {} and shop admin {}", 
                request.getCustomerId(), request.getShopAdminId());
        
        ChatRoom chatRoom = chatRoomRepository
                .findByCustomerIdAndShopAdminId(request.getCustomerId(), request.getShopAdminId())
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setCustomerId(request.getCustomerId());
                    newRoom.setCustomerName(request.getCustomerName());
                    newRoom.setShopAdminId(request.getShopAdminId());
                    newRoom.setShopAdminName(request.getShopAdminName());
                    newRoom.setShopId(request.getShopId());
                    newRoom.setShopName(request.getShopName());
                    return chatRoomRepository.save(newRoom);
                });
        
        return convertToConversationDTO(chatRoom);
    }
    
    /**
     * Send a message
     */
    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request) {
        log.info("Sending message in chat room {}", request.getChatRoomId());
        log.info("Sender type: {}, Sender ID: {}", request.getSenderType(), request.getSenderId());
        
        // Create message
        ChatMessage message = new ChatMessage();
        message.setChatRoomId(request.getChatRoomId());
        message.setSenderId(request.getSenderId());
        message.setSenderName(request.getSenderName());
        message.setSenderType(ChatMessage.SenderType.valueOf(request.getSenderType()));
        message.setMessageText(request.getMessageText());
        message.setStatus(ChatMessage.MessageStatus.SENT);
        
        // Check if this is the first message from customer BEFORE saving
        boolean isFirstCustomerMessage = false;
        if ("CUSTOMER".equals(request.getSenderType())) {
            long messageCount = chatMessageRepository.countByChatRoomIdAndSenderType(
                    request.getChatRoomId(), 
                    ChatMessage.SenderType.CUSTOMER
            );
            log.info("Customer message count in chat room {} BEFORE save: {}", request.getChatRoomId(), messageCount);
            isFirstCustomerMessage = (messageCount == 0); // Check before saving
            log.info("Is first customer message: {}", isFirstCustomerMessage);
        }
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        log.info("Message saved with ID: {}", savedMessage.getId());
        
        // Update chat room
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        chatRoom.setLastMessage(request.getMessageText().substring(0, Math.min(100, request.getMessageText().length())));
        chatRoom.setLastMessageTime(LocalDateTime.now());
        
        // Increment unread count for recipient
        if ("CUSTOMER".equals(request.getSenderType())) {
            chatRoom.setUnreadCountAdmin(chatRoom.getUnreadCountAdmin() + 1);
        } else {
            chatRoom.setUnreadCountCustomer(chatRoom.getUnreadCountCustomer() + 1);
        }
        
        chatRoomRepository.save(chatRoom);
        
        // Convert to DTO
        MessageDTO messageDTO = convertToMessageDTO(savedMessage);
        
        // Send via WebSocket to chat room subscribers
        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId(), messageDTO);
        
        // Send push notification to recipient
        sendMessageNotification(chatRoom, request.getSenderName(), request.getMessageText(), request.getSenderType());
        
        // Send automatic welcome message if this is customer's first message
        if (isFirstCustomerMessage) {
            log.info("Triggering automatic welcome message for chat room {}", chatRoom.getId());
            // Use a separate thread to avoid blocking and ensure the first message is fully processed
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Small delay to ensure first message is processed
                    sendAutomaticWelcomeMessage(chatRoom);
                } catch (InterruptedException e) {
                    log.error("Welcome message thread interrupted", e);
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        return messageDTO;
    }
    
    /**
     * Send automatic welcome message from shop admin
     */
    private void sendAutomaticWelcomeMessage(ChatRoom chatRoom) {
        try {
            log.info("Sending automatic welcome message in chat room {}", chatRoom.getId());
            
            String welcomeMessage = String.format(
                "Hello! Thank you for contacting %s.\n\n" +
                "How can we help you today? We'll get back to you as soon as possible.\n\n" +
                "Feel free to ask about:\n" +
                "• Available services\n" +
                "• Appointment scheduling\n" +
                "• Pricing information\n" +
                "• Any other questions",
                chatRoom.getShopName()
            );
            
            // Create auto-reply message
            ChatMessage autoReply = new ChatMessage();
            autoReply.setChatRoomId(chatRoom.getId());
            autoReply.setSenderId(chatRoom.getShopAdminId());
            autoReply.setSenderName(chatRoom.getShopAdminName());
            autoReply.setSenderType(ChatMessage.SenderType.ADMIN);
            autoReply.setMessageText(welcomeMessage);
            autoReply.setStatus(ChatMessage.MessageStatus.SENT);
            
            ChatMessage savedAutoReply = chatMessageRepository.save(autoReply);
            
            // Update chat room
            chatRoom.setLastMessage(welcomeMessage.substring(0, Math.min(100, welcomeMessage.length())));
            chatRoom.setLastMessageTime(LocalDateTime.now());
            chatRoom.setUnreadCountCustomer(chatRoom.getUnreadCountCustomer() + 1);
            chatRoomRepository.save(chatRoom);
            
            // Convert to DTO and send via WebSocket
            MessageDTO autoReplyDTO = convertToMessageDTO(savedAutoReply);
            messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getId(), autoReplyDTO);
            
            // Send push notification to customer
            try {
                notificationService.sendPushNotification(
                        chatRoom.getCustomerId(),
                        "CUSTOMER",
                        chatRoom.getShopName() + " replied",
                        "We're here to help! How can we assist you today?",
                        java.util.Map.of(
                                "type", "CHAT",
                                "chatRoomId", chatRoom.getId().toString(),
                                "senderName", chatRoom.getShopAdminName()
                        )
                );
            } catch (Exception e) {
                log.warn("Failed to send welcome message notification: {}", e.getMessage());
            }
            
            log.info("Automatic welcome message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send automatic welcome message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get all messages in a chat room
     */
    public List<MessageDTO> getMessages(Long chatRoomId) {
        log.info("Getting messages for chat room {}", chatRoomId);
        
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId);
        return messages.stream()
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all chat rooms for a user
     */
    public List<ConversationDTO> getChatRooms(Long userId, String userType) {
        log.info("Getting chat rooms for user {} (type: {})", userId, userType);
        
        List<ChatRoom> chatRooms;
        if ("CUSTOMER".equals(userType)) {
            chatRooms = chatRoomRepository.findByCustomerIdOrderByLastMessageTimeDesc(userId);
        } else {
            chatRooms = chatRoomRepository.findByShopAdminIdOrderByLastMessageTimeDesc(userId);
        }
        
        return chatRooms.stream()
                .map(this::convertToConversationDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long chatRoomId, String userType) {
        log.info("Marking messages as read in chat room {} for user type {}", chatRoomId, userType);
        
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        // Reset unread count
        if ("CUSTOMER".equals(userType)) {
            chatRoom.setUnreadCountCustomer(0);
            // Update message status for messages sent by admin
            chatMessageRepository.updateMessageStatus(
                    chatRoomId, 
                    ChatMessage.MessageStatus.READ,
                    ChatMessage.SenderType.CUSTOMER
            );
        } else {
            chatRoom.setUnreadCountAdmin(0);
            // Update message status for messages sent by customer
            chatMessageRepository.updateMessageStatus(
                    chatRoomId, 
                    ChatMessage.MessageStatus.READ,
                    ChatMessage.SenderType.ADMIN
            );
        }
        
        chatRoomRepository.save(chatRoom);
        
        // Notify via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/read", userType);
    }
    
    /**
     * Update typing status
     */
    @Transactional
    public void updateTypingStatus(TypingStatusRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        if ("CUSTOMER".equals(request.getUserType())) {
            chatRoom.setCustomerTyping(request.getIsTyping());
        } else {
            chatRoom.setAdminTyping(request.getIsTyping());
        }
        
        chatRoomRepository.save(chatRoom);
        
        // Notify via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + request.getChatRoomId() + "/typing", request);
    }
    
    /**
     * Update online status
     */
    @Transactional
    public void updateOnlineStatus(Long chatRoomId, String userType, Boolean isOnline) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        
        if ("CUSTOMER".equals(userType)) {
            chatRoom.setCustomerOnline(isOnline);
        } else {
            chatRoom.setAdminOnline(isOnline);
        }
        
        chatRoomRepository.save(chatRoom);
        
        // Notify via WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoomId + "/online", 
                new OnlineStatusUpdate(userType, isOnline));
    }
    
    /**
     * Get total unread count for user
     */
    public Integer getUnreadCount(Long userId, String userType) {
        List<ChatRoom> chatRooms;
        if ("CUSTOMER".equals(userType)) {
            chatRooms = chatRoomRepository.findByCustomerIdOrderByLastMessageTimeDesc(userId);
            return chatRooms.stream()
                    .mapToInt(ChatRoom::getUnreadCountCustomer)
                    .sum();
        } else {
            chatRooms = chatRoomRepository.findByShopAdminIdOrderByLastMessageTimeDesc(userId);
            return chatRooms.stream()
                    .mapToInt(ChatRoom::getUnreadCountAdmin)
                    .sum();
        }
    }
    
    // Helper methods
    
    private MessageDTO convertToMessageDTO(ChatMessage message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setChatRoomId(message.getChatRoomId());
        dto.setSenderId(message.getSenderId());
        dto.setSenderName(message.getSenderName());
        dto.setSenderType(message.getSenderType().name());
        dto.setMessageText(message.getMessageText());
        dto.setStatus(message.getStatus().name());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
    
    private ConversationDTO convertToConversationDTO(ChatRoom chatRoom) {
        ConversationDTO dto = new ConversationDTO();
        dto.setId(chatRoom.getId());
        dto.setCustomerId(chatRoom.getCustomerId());
        dto.setCustomerName(chatRoom.getCustomerName());
        dto.setShopAdminId(chatRoom.getShopAdminId());
        dto.setShopAdminName(chatRoom.getShopAdminName());
        dto.setShopId(chatRoom.getShopId());
        dto.setShopName(chatRoom.getShopName());
        dto.setLastMessage(chatRoom.getLastMessage());
        dto.setLastMessageTime(chatRoom.getLastMessageTime());
        dto.setUnreadCountCustomer(chatRoom.getUnreadCountCustomer());
        dto.setUnreadCountAdmin(chatRoom.getUnreadCountAdmin());
        dto.setCustomerOnline(chatRoom.getCustomerOnline());
        dto.setAdminOnline(chatRoom.getAdminOnline());
        dto.setCustomerTyping(chatRoom.getCustomerTyping());
        dto.setAdminTyping(chatRoom.getAdminTyping());
        dto.setCreatedAt(chatRoom.getCreatedAt());
        dto.setUpdatedAt(chatRoom.getUpdatedAt());
        return dto;
    }
    
    private void sendMessageNotification(ChatRoom chatRoom, String senderName, String messageText, String senderType) {
        try {
            Long recipientId = "CUSTOMER".equals(senderType) ? chatRoom.getShopAdminId() : chatRoom.getCustomerId();
            String recipientType = "CUSTOMER".equals(senderType) ? "ADMIN" : "CUSTOMER";
            
            String title = "New message from " + senderName;
            String body = messageText.substring(0, Math.min(100, messageText.length()));
            
            notificationService.sendPushNotification(
                    recipientId,
                    recipientType,
                    title,
                    body,
                    java.util.Map.of(
                            "type", "CHAT",
                            "chatRoomId", chatRoom.getId().toString(),
                            "senderName", senderName
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send chat notification", e);
        }
    }
    
    // Inner class for online status updates
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class OnlineStatusUpdate {
        private String userType;
        private Boolean isOnline;
    }
}

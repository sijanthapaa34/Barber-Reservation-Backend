package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.chat.*;
import com.sijan.barberReservation.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {
    
    private final ChatService chatService;
    
    /**
     * Get or create a chat room
     */
    @PostMapping("/rooms")
    public ResponseEntity<ConversationDTO> getOrCreateChatRoom(@RequestBody CreateChatRequest request) {
        log.info("REST: Get or create chat room");
        ConversationDTO conversation = chatService.getOrCreateChatRoom(request);
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * Get all chat rooms for a user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ConversationDTO>> getChatRooms(
            @RequestParam Long userId,
            @RequestParam String userType) {
        log.info("REST: Get chat rooms for user {} (type: {})", userId, userType);
        List<ConversationDTO> conversations = chatService.getChatRooms(userId, userType);
        return ResponseEntity.ok(conversations);
    }
    
    /**
     * Send a message (REST endpoint)
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody SendMessageRequest request) {
        log.info("REST: Send message to chat room {}", request.getChatRoomId());
        MessageDTO message = chatService.sendMessage(request);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Get all messages in a chat room
     */
    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatRoomId) {
        log.info("REST: Get messages for chat room {}", chatRoomId);
        List<MessageDTO> messages = chatService.getMessages(chatRoomId);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Mark messages as read
     */
    @PostMapping("/messages/{chatRoomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatRoomId,
            @RequestParam String userType) {
        log.info("REST: Mark messages as read in chat room {} for user type {}", chatRoomId, userType);
        chatService.markMessagesAsRead(chatRoomId, userType);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update typing status
     */
    @PostMapping("/typing")
    public ResponseEntity<Void> updateTypingStatus(@RequestBody TypingStatusRequest request) {
        log.info("REST: Update typing status");
        chatService.updateTypingStatus(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update online status
     */
    @PostMapping("/online")
    public ResponseEntity<Void> updateOnlineStatus(
            @RequestParam Long chatRoomId,
            @RequestParam String userType,
            @RequestParam Boolean isOnline) {
        log.info("REST: Update online status");
        chatService.updateOnlineStatus(chatRoomId, userType, isOnline);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get total unread count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam String userType) {
        log.info("REST: Get unread count for user {} (type: {})", userId, userType);
        Integer count = chatService.getUnreadCount(userId, userType);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
    
    // WebSocket message handlers
    
    /**
     * Send message via WebSocket
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessageViaWebSocket(@Payload SendMessageRequest request) {
        log.info("WebSocket: Send message to chat room {}", request.getChatRoomId());
        chatService.sendMessage(request);
    }
    
    /**
     * Update typing status via WebSocket
     */
    @MessageMapping("/chat.typing")
    public void updateTypingStatusViaWebSocket(@Payload TypingStatusRequest request) {
        log.info("WebSocket: Update typing status");
        chatService.updateTypingStatus(request);
    }
}

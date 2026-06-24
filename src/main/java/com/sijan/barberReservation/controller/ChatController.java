package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.chat.*;
import com.sijan.barberReservation.config.WebSocketEventListener;
import com.sijan.barberReservation.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    private final WebSocketEventListener webSocketEventListener;

    @PostMapping("/rooms")
    public ResponseEntity<ConversationDTO> getOrCreateChatRoom(@RequestBody CreateChatRequest request) {
        log.info("REST: Get or create chat room for customer {} and admin {}",
                request.getCustomerId(), request.getShopAdminId());
        ConversationDTO conversation = chatService.getOrCreateChatRoom(request);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ConversationDTO>> getChatRooms(
            @RequestParam Long userId,
            @RequestParam String userType) {
        log.info("REST: Get chat rooms for user {} (type: {})", userId, userType);
        List<ConversationDTO> conversations = chatService.getChatRooms(userId, userType);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/messages/{chatRoomId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatRoomId) {
        log.info("REST: Get messages for chat room {}", chatRoomId);
        List<MessageDTO> messages = chatService.getMessages(chatRoomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount(
            @RequestParam Long userId,
            @RequestParam String userType) {
        log.info("REST: Get unread count for user {} (type: {})", userId, userType);
        Integer count = chatService.getUnreadCount(userId, userType);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @MessageMapping("/chat.join")
    public void joinChatRoom(@Payload OnlineStatusRequest request,
                             SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket: User {} joining chat room {}", request.getUserType(), request.getChatRoomId());

        // Register session for disconnect handling
        webSocketEventListener.registerSession(sessionId, request.getChatRoomId(), request.getUserType());

        // Set user as online
        chatService.updateOnlineStatus(request.getChatRoomId(), request.getUserType(), true);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessageViaWebSocket(@Payload SendMessageRequest request) {
        log.info("WebSocket: Send message to chat room {} from {}",
                request.getChatRoomId(), request.getSenderType());
        chatService.sendMessage(request);
    }

    @MessageMapping("/chat.typing")
    public void updateTypingStatusViaWebSocket(@Payload TypingStatusRequest request) {
        log.info("WebSocket: Update typing status for {} in room {}",
                request.getUserType(), request.getChatRoomId());
        chatService.updateTypingStatus(request);
    }

    @MessageMapping("/chat.read")
    public void markMessagesAsReadViaWebSocket(@Payload ReadStatusRequest request) {
        log.info("WebSocket: Mark messages as read for {} in room {}",
                request.getUserType(), request.getChatRoomId());
        chatService.markMessagesAsRead(request.getChatRoomId(), request.getUserType());
    }

    @MessageMapping("/chat.leave")
    public void leaveChatRoom(@Payload OnlineStatusRequest request,
                              SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("WebSocket: User {} leaving chat room {}", request.getUserType(), request.getChatRoomId());

        // Unregister session
        webSocketEventListener.unregisterSession(sessionId);

        // Set user as offline
        chatService.updateOnlineStatus(request.getChatRoomId(), request.getUserType(), false);
    }
}
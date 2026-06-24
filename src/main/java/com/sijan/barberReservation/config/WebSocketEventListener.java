package com.sijan.barberReservation.config;

import com.sijan.barberReservation.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatService chatService;

    // Store session info: sessionId -> {chatRoomId, userType}
    private final Map<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection established");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.info("WebSocket connection disconnected: {}", sessionId);

        SessionInfo sessionInfo = activeSessions.remove(sessionId);
        if (sessionInfo != null) {
            // Update online status to offline when disconnected
            chatService.updateOnlineStatus(sessionInfo.chatRoomId, sessionInfo.userType, false);
        }
    }

    public void registerSession(String sessionId, Long chatRoomId, String userType) {
        activeSessions.put(sessionId, new SessionInfo(chatRoomId, userType));
        log.info("Session registered: {} -> chatRoom: {}, userType: {}", sessionId, chatRoomId, userType);
    }

    public void unregisterSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    private record SessionInfo(Long chatRoomId, String userType) {}
}
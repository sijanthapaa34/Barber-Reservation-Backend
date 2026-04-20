// src/main/java/com/sijan/barberReservation/controller/NotificationController.java
package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.notification.NotificationResponse;
import com.sijan.barberReservation.model.Notification;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.repository.NotificationRepository;
import com.sijan.barberReservation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @PostMapping("/token")
    public ResponseEntity<Void> saveToken(
            @RequestBody String token,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        String userType = userPrincipal.getAuthorities().iterator().next().getAuthority();
        notificationService.saveToken(userPrincipal.getId(), token, userType.replace("ROLE_", ""));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByTimestampDesc(userPrincipal.getId());

        List<NotificationResponse> response = notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userPrincipal.getId());
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(userPrincipal.getId()))
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<Notification> unreadNotifications = notificationRepository
                .findUnreadByUserId(userPrincipal.getId());

        unreadNotifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unreadNotifications);

        return ResponseEntity.ok().build();
    }

    /**
     * Send chat message notification
     */
    @PostMapping("/chat")
    public ResponseEntity<Void> sendChatNotification(
            @RequestBody Map<String, String> payload
    ) {
        Long recipientId = Long.parseLong(payload.get("recipientId"));
        String recipientType = payload.get("recipientType");
        String senderName = payload.get("senderName");
        String messagePreview = payload.get("messagePreview");
        String chatId = payload.get("chatId");

        String title = "New message from " + senderName;
        String body = messagePreview;

        notificationService.sendPushNotification(
            recipientId,
            recipientType,
            title,
            body,
            Map.of(
                "type", "CHAT_MESSAGE",
                "chatId", chatId,
                "senderName", senderName
            )
        );

        return ResponseEntity.ok().build();
    }

    private NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setRead(notification.isRead());
        response.setTimestamp(notification.getTimestamp());
        return response;
    }
}
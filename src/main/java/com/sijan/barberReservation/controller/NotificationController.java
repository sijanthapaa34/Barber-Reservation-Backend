//package com.sijan.barberReservation.controller;
//
//import com.sijan.barberReservation.DTO.notification.NotificationDTO;
//import com.sijan.barberReservation.service.NotificationService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/notifications")
//public class NotificationController {
//
//    private final NotificationService notificationService;
//
//    public NotificationController(NotificationService notificationService) {
//        this.notificationService = notificationService;
//    }
//
//    // GET /api/notifications/me - Get all notifications for current user
//    @GetMapping("/me")
//    public ResponseEntity<List<NotificationDTO>> getMyNotifications(
//            @RequestHeader("X-User-ID") Long userId) {
//
//        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
//        return ResponseEntity.ok(notifications);
//    }
//
//    // PUT /api/notifications/{id}/read - Mark notification as read
//    @PutMapping("/{id}/read")
//    public ResponseEntity<Void> markAsRead(
//            @PathVariable Long id,
//            @RequestHeader("X-User-ID") Long userId) {
//
//        notificationService.markAsRead(id, userId);
//        return ResponseEntity.ok().build();
//    }
//}

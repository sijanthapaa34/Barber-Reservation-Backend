// src/main/java/com/sijan/barberReservation/service/NotificationService.java
package com.sijan.barberReservation.service;

import com.google.firebase.messaging.*;
import com.sijan.barberReservation.model.Notification;
import com.sijan.barberReservation.model.NotificationToken;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.repository.NotificationRepository;
import com.sijan.barberReservation.repository.NotificationTokenRepository;
import com.sijan.barberReservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationTokenRepository tokenRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Save or update FCM token for a user
     */
    @Transactional
    public void saveToken(Long userId, String token, String userType) {
        log.info("=== Saving FCM Token ===");
        log.info("User ID: {}", userId);
        log.info("User Type: {}", userType);
        log.info("Token: {}", token);
        
        // Delete ALL existing tokens for this user (not just by token)
        // This prevents duplicate token errors
        List<NotificationToken> existingTokens = tokenRepository.findByUserIdAndUserType(userId, userType);
        if (!existingTokens.isEmpty()) {
            log.info("Deleting {} existing token(s) for user: {}", existingTokens.size(), userId);
            tokenRepository.deleteAll(existingTokens);
        }
        
        // Also delete any tokens with the same token string (in case it was registered to another user)
        tokenRepository.findByToken(token).ifPresent(existing -> {
            log.info("Deleting duplicate token registered to user: {}", existing.getUserId());
            tokenRepository.delete(existing);
        });

        NotificationToken newToken = new NotificationToken();
        newToken.setUserId(userId);
        newToken.setToken(token);
        newToken.setUserType(userType);
        tokenRepository.save(newToken);

        log.info("Successfully saved FCM token for user: {}, type: {}", userId, userType);
        log.info("=======================");
    }

    /**
     * Send push notification and save to database
     */
    @Transactional
    public void sendPushNotification(Long userId, String userType, String title, String body, Map<String, String> additionalData) {
        log.info("=== Sending Push Notification ===");
        log.info("User ID: {}", userId);
        log.info("User Type: {}", userType);
        log.info("Title: {}", title);
        log.info("Body: {}", body);
        
        List<NotificationToken> tokens = tokenRepository.findByUserIdAndUserType(userId, userType);

        if (tokens.isEmpty()) {
            log.warn("No device tokens found for user: {}, type: {}", userId, userType);
            log.warn("Push notification will NOT be delivered to device!");
            log.info("=================================");
            // Still save notification to database
            saveNotificationToDb(userId, title, body);
            return;
        }

        log.info("Found {} device token(s) for user", tokens.size());
        tokens.forEach(t -> log.info("   Token: {}...", t.getToken().substring(0, Math.min(20, t.getToken().length()))));

        // FIX: Use HTTP v1 API with individual send() instead of deprecated sendMulticast()
        com.google.firebase.messaging.Notification notification = com.google.firebase.messaging.Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        // Build data payload
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("type", additionalData != null && additionalData.containsKey("type") ? additionalData.get("type") : "GENERAL");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        if (additionalData != null) {
            log.info("Additional data: {}", additionalData);
            data.putAll(additionalData);
        }

        // Send to each token individually using HTTP v1 API
        int successCount = 0;
        int failureCount = 0;
        List<NotificationToken> tokensToDelete = new ArrayList<>();

        for (NotificationToken token : tokens) {
            try {
                Message message = Message.builder()
                        .setNotification(notification)
                        .putAllData(data)
                        .setToken(token.getToken())
                        .build();

                String response = FirebaseMessaging.getInstance().send(message);
                log.info("Successfully sent notification to token: {}..., response: {}", 
                    token.getToken().substring(0, Math.min(20, token.getToken().length())), response);
                successCount++;
            } catch (FirebaseMessagingException e) {
                log.error("Failed to send to token: {}..., error: {}", 
                    token.getToken().substring(0, Math.min(20, token.getToken().length())), e.getMessage());
                failureCount++;
                
                // Mark invalid tokens for deletion
                if (e.getMessage().contains("not registered") || e.getMessage().contains("InvalidRegistration")) {
                    tokensToDelete.add(token);
                }
            }
        }

        // Clean up invalid tokens
        if (!tokensToDelete.isEmpty()) {
            log.info("Removing {} invalid token(s)", tokensToDelete.size());
            tokenRepository.deleteAll(tokensToDelete);
        }

        log.info("Push notification summary: {} sent, {} failed", successCount, failureCount);
        log.info("=================================");

        // Save notification to database for in-app notification history
        saveNotificationToDb(userId, title, body);
    }

    /**
     * Overloaded method without additional data
     */
    public void sendPushNotification(Long userId, String userType, String title, String body) {
        sendPushNotification(userId, userType, title, body, null);
    }

    /**
     * Save notification to database
     */
    private void saveNotificationToDb(Long userId, String title, String body) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setTitle(title);
                notification.setMessage(body);
                notificationRepository.save(notification);
            }
        } catch (Exception e) {
            log.error("Error saving notification to DB", e);
        }
    }

    /**
     * Remove invalid/expired FCM tokens
     */
    private void cleanInvalidTokens(List<NotificationToken> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
                String error = sendResponse.getException().getMessage();
                if (error.contains("not registered") || error.contains("InvalidRegistration")) {
                    tokenRepository.delete(tokens.get(i));
                    log.info("Removed invalid token for user: {}", tokens.get(i).getUserId());
                }
            }
        }
    }

    // ==================== SPECIFIC NOTIFICATION METHODS ====================

    /**
     * Send appointment booking notification to CUSTOMER
     */
    public void sendAppointmentBookedToCustomer(Long customerId, String shopName, String barberName, String scheduledTime) {
        String title = "Appointment Confirmed";
        String body = String.format("Your appointment at %s with %s on %s has been confirmed.",
                shopName, barberName, scheduledTime);

        sendPushNotification(customerId, "CUSTOMER", title, body,
                Map.of(
                        "type", "APPOINTMENT_BOOKED",
                        "shopName", shopName,
                        "barberName", barberName
                ));
    }

    /**
     * Send appointment booking notification to BARBER
     */
    public void sendNewAppointmentToBarber(Long barberId, String customerName, String serviceName, String scheduledTime) {
        String title = "New Booking";
        String body = String.format("You have a new appointment with %s for %s at %s.",
                customerName, serviceName, scheduledTime);

        sendPushNotification(barberId, "BARBER", title, body,
                Map.of(
                        "type", "NEW_APPOINTMENT",
                        "customerName", customerName,
                        "serviceName", serviceName
                ));
    }

    /**
     * Send review submitted notification to BARBER
     */
    public void sendReviewSubmittedToBarber(Long barberId, String customerName, Integer rating) {
        String title = "New Review";
        String body = String.format("%s left you a %d-star review. Tap to see what they said!",
                customerName, rating);

        sendPushNotification(barberId, "BARBER", title, body,
                Map.of(
                        "type", "NEW_REVIEW",
                        "customerName", customerName,
                        "rating", String.valueOf(rating)
                ));
    }

    /**
     * Send review reply notification to CUSTOMER
     */
    public void sendReviewReplyToCustomer(Long customerId, String barberName, String barberShopName) {
        String title = "Review Reply";
        String body = String.format("%s from %s replied to your review. Tap to see their response!",
                barberName, barberShopName);

        sendPushNotification(customerId, "CUSTOMER", title, body,
                Map.of(
                        "type", "REVIEW_REPLY",
                        "barberName", barberName,
                        "shopName", barberShopName
                ));
    }

    /**
     * Send application submitted notification to ADMIN
     */
    public void sendApplicationSubmittedToAdmin(Long adminId, String applicantName, String applicationType) {
        String title = "New Application";
        String body = String.format("%s submitted a %s application. Review it now!",
                applicantName, applicationType);

        sendPushNotification(adminId, "ADMIN", title, body,
                Map.of(
                        "type", "NEW_APPLICATION",
                        "applicantName", applicantName,
                        "applicationType", applicationType
                ));
    }

    /**
     * Send application status update by email (for applicants who may now be registered users)
     */
    public void sendApplicationStatusUpdate(Long applicationId, String status, String applicationType) {
        // No-op: applicant may not be a registered user yet (PENDING state)
        // Notification is sent via email by EmailService
        // For APPROVED state, the user is now registered — but we don't have their userId here
        // This is handled by the push token they register after first login
        log.info("Application {} status updated to {} (push notification skipped - user not yet registered)", applicationId, status);
    }

    /**
     * Send appointment reminder notification (45 minutes before)
     */
    public void sendAppointmentReminder(Long customerId, String shopName, String barberName, String scheduledTime) {
        String title = "Appointment Reminder";
        String body = String.format("Your appointment at %s with %s is in 45 minutes (%s). Don't forget!",
                shopName, barberName, scheduledTime);

        sendPushNotification(customerId, "CUSTOMER", title, body,
                Map.of(
                        "type", "APPOINTMENT_REMINDER",
                        "shopName", shopName,
                        "barberName", barberName,
                        "scheduledTime", scheduledTime
                ));
    }

    /**
     * Send appointment cancelled notification
     */
    public void sendAppointmentCancelled(Long userId, String userType, String cancelledBy, String shopName) {
        String title = "Appointment Cancelled";
        String body = String.format("Your appointment at %s has been cancelled by %s.", shopName, cancelledBy);

        sendPushNotification(userId, userType, title, body,
                Map.of(
                        "type", "APPOINTMENT_CANCELLED",
                        "cancelledBy", cancelledBy,
                        "shopName", shopName
                ));
    }

    /**
     * Send appointment rescheduled notification
     */
    public void sendAppointmentRescheduled(Long userId, String userType, String newTime, String shopName) {
        String title = "Appointment Rescheduled";
        String body = String.format("Your appointment at %s has been rescheduled to %s.", shopName, newTime);

        sendPushNotification(userId, userType, title, body,
                Map.of(
                        "type", "APPOINTMENT_RESCHEDULED",
                        "newTime", newTime,
                        "shopName", shopName
                ));
    }

    /**
     * Send payment completed notification to CUSTOMER
     */
    public void sendPaymentCompletedToCustomer(Long customerId, String shopName, String amount, String paymentMethod) {
        String title = "Payment Successful";
        String body = String.format("Your payment of Rs. %s via %s for %s has been confirmed.",
                amount, paymentMethod, shopName);

        sendPushNotification(customerId, "CUSTOMER", title, body,
                Map.of(
                        "type", "APPOINTMENT_BOOKED",
                        "shopName", shopName,
                        "amount", amount,
                        "paymentMethod", paymentMethod
                ));
    }

    /**
     * Send barber application approved notification
     */
    public void sendBarberApplicationApproved(Long barberId, String shopName) {
        String title = "Application Approved";
        String body = String.format("Congratulations! Your application to join %s has been approved. You can now start accepting appointments.",
                shopName);

        sendPushNotification(barberId, "BARBER", title, body,
                Map.of(
                        "type", "APPLICATION_STATUS",
                        "status", "APPROVED",
                        "shopName", shopName
                ));
    }

    /**
     * Send barber application rejected notification
     */
    public void sendBarberApplicationRejected(Long barberId, String shopName, String reason) {
        String title = "Application Update";
        String body = String.format("Your application to join %s was not approved at this time. %s",
                shopName, reason != null && !reason.isEmpty() ? "Reason: " + reason : "");

        sendPushNotification(barberId, "BARBER", title, body,
                Map.of(
                        "type", "APPLICATION_STATUS",
                        "status", "REJECTED",
                        "shopName", shopName,
                        "reason", reason != null ? reason : ""
                ));
    }

    /**
     * Send shop application approved notification to SHOP_ADMIN
     */
    public void sendShopApplicationApproved(Long shopAdminId, String shopName) {
        String title = "Shop Approved";
        String body = String.format("Congratulations! Your shop '%s' has been approved and is now live on FadeBook.",
                shopName);

        sendPushNotification(shopAdminId, "ADMIN", title, body,
                Map.of(
                        "type", "APPLICATION_STATUS",
                        "status", "APPROVED",
                        "shopName", shopName
                ));
    }

    /**
     * Send shop application rejected notification to SHOP_ADMIN
     */
    public void sendShopApplicationRejected(Long shopAdminId, String shopName, String reason) {
        String title = "Application Update";
        String body = String.format("Your shop application for '%s' was not approved at this time. %s",
                shopName, reason != null && !reason.isEmpty() ? "Reason: " + reason : "");

        sendPushNotification(shopAdminId, "ADMIN", title, body,
                Map.of(
                        "type", "APPLICATION_STATUS",
                        "status", "REJECTED",
                        "shopName", shopName,
                        "reason", reason != null ? reason : ""
                ));
    }

    // ==================== SHOP ADMIN NOTIFICATIONS ====================

    public void sendNewAppointmentToShopAdmin(Long shopAdminId, String customerName, String barberName, String scheduledTime) {
        String title = "New Appointment Booked";
        String body = String.format("%s booked with %s on %s.", customerName, barberName, scheduledTime);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPOINTMENT", "customerName", customerName));
    }

    public void sendAppointmentCancelledToShopAdmin(Long shopAdminId, String customerName, String barberName) {
        String title = "Appointment Cancelled";
        String body = String.format("%s cancelled their appointment with %s.", customerName, barberName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "APPOINTMENT_CANCELLED", "customerName", customerName));
    }

    public void sendBarberApplicationToShopAdmin(Long shopAdminId, String applicantName) {
        String title = "New Barber Application";
        String body = String.format("%s applied to join your shop. Review their application now.", applicantName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", applicantName));
    }

    public void sendNewReviewToShopAdmin(Long shopAdminId, String customerName, int rating) {
        String title = "New Review Received";
        String body = String.format("%s left a %d-star review on your shop.", customerName, rating);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_REVIEW", "customerName", customerName, "rating", String.valueOf(rating)));
    }

    public void sendBarberJoinedToShopAdmin(Long shopAdminId, String barberName) {
        String title = "New Barber Joined";
        String body = String.format("%s has joined your shop.", barberName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "barberName", barberName));
    }

    public void sendServiceAddedToShopAdmin(Long shopAdminId, String serviceName) {
        String title = "New Service Added";
        String body = String.format("Service '%s' has been added to your shop.", serviceName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "serviceName", serviceName));
    }

    // ==================== MAIN ADMIN NOTIFICATIONS ====================

    public void sendShopApplicationToMainAdmin(Long mainAdminId, String shopName, String ownerName) {
        String title = "New Shop Application";
        String body = String.format("%s applied to open '%s'. Review it now.", ownerName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", ownerName));
    }

    public void sendBarberApplicationToMainAdmin(Long mainAdminId, String barberName, String shopName) {
        String title = "Barber Application Awaiting Approval";
        String body = String.format("%s's application for %s is ready for your final review.", barberName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", barberName));
    }

    public void sendShopRegisteredToMainAdmin(Long mainAdminId, String shopName) {
        String title = "New Shop Registered";
        String body = String.format("'%s' is now live on FadeBook.", shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "shopName", shopName));
    }

    public void sendBarberRegisteredToMainAdmin(Long mainAdminId, String barberName, String shopName) {
        String title = "New Barber Registered";
        String body = String.format("%s is now active at %s.", barberName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "barberName", barberName));
    }
}

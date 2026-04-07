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
        // Delete existing token if it exists (for token refresh)
        tokenRepository.findByToken(token).ifPresent(existing -> {
            tokenRepository.delete(existing);
        });

        NotificationToken newToken = new NotificationToken();
        newToken.setUserId(userId);
        newToken.setToken(token);
        newToken.setUserType(userType);
        tokenRepository.save(newToken);

        log.info("Saved FCM token for user: {}, type: {}", userId, userType);
    }

    /**
     * Send push notification and save to database
     */
    @Transactional
    public void sendPushNotification(Long userId, String userType, String title, String body, Map<String, String> additionalData) {
        List<NotificationToken> tokens = tokenRepository.findByUserIdAndUserType(userId, userType);

        if (tokens.isEmpty()) {
            log.info("No device tokens found for user: {}, type: {}", userId, userType);
            // Still save notification to database
            saveNotificationToDb(userId, title, body);
            return;
        }

        // Build message with data payload
        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("type", additionalData != null && additionalData.containsKey("type") ? additionalData.get("type") : "GENERAL")
                .putData("timestamp", String.valueOf(System.currentTimeMillis()));

        // Add additional data if provided
        if (additionalData != null) {
            additionalData.forEach(messageBuilder::putData);
        }

        messageBuilder.addAllTokens(tokens.stream().map(NotificationToken::getToken).toList());

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(messageBuilder.build());
            log.info("Successfully sent {} messages to user: {}", response.getSuccessCount(), userId);

            // Clean up invalid tokens
            if (response.getFailureCount() > 0) {
                cleanInvalidTokens(tokens, response);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM to user: {}", userId, e);
        }

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
        String title = "Appointment Confirmed! ✅";
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
        String title = "New Booking! 📅";
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
        String title = "New Review! ⭐";
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
        String title = "Review Reply! 💬";
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
        String title = "New Application! 📋";
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

    // ==================== SHOP ADMIN NOTIFICATIONS ====================

    public void sendNewAppointmentToShopAdmin(Long shopAdminId, String customerName, String barberName, String scheduledTime) {
        String title = "New Appointment Booked \uD83D\uDCC5";
        String body = String.format("%s booked with %s on %s.", customerName, barberName, scheduledTime);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPOINTMENT", "customerName", customerName));
    }

    public void sendAppointmentCancelledToShopAdmin(Long shopAdminId, String customerName, String barberName) {
        String title = "Appointment Cancelled \u274C";
        String body = String.format("%s cancelled their appointment with %s.", customerName, barberName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "APPOINTMENT_CANCELLED", "customerName", customerName));
    }

    public void sendBarberApplicationToShopAdmin(Long shopAdminId, String applicantName) {
        String title = "New Barber Application \uD83D\uDCCB";
        String body = String.format("%s applied to join your shop. Review their application now.", applicantName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", applicantName));
    }

    public void sendNewReviewToShopAdmin(Long shopAdminId, String customerName, int rating) {
        String title = "New Review Received \u2B50";
        String body = String.format("%s left a %d-star review on your shop.", customerName, rating);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "NEW_REVIEW", "customerName", customerName, "rating", String.valueOf(rating)));
    }

    public void sendBarberJoinedToShopAdmin(Long shopAdminId, String barberName) {
        String title = "New Barber Joined \u2702\uFE0F";
        String body = String.format("%s has joined your shop.", barberName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "barberName", barberName));
    }

    public void sendServiceAddedToShopAdmin(Long shopAdminId, String serviceName) {
        String title = "New Service Added \uD83D\uDC88";
        String body = String.format("Service '%s' has been added to your shop.", serviceName);
        sendPushNotification(shopAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "serviceName", serviceName));
    }

    // ==================== MAIN ADMIN NOTIFICATIONS ====================

    public void sendShopApplicationToMainAdmin(Long mainAdminId, String shopName, String ownerName) {
        String title = "New Shop Application \uD83C\uDFEA";
        String body = String.format("%s applied to open '%s'. Review it now.", ownerName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", ownerName));
    }

    public void sendBarberApplicationToMainAdmin(Long mainAdminId, String barberName, String shopName) {
        String title = "Barber Application Awaiting Approval \uD83D\uDCCB";
        String body = String.format("%s's application for %s is ready for your final review.", barberName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "NEW_APPLICATION", "applicantName", barberName));
    }

    public void sendShopRegisteredToMainAdmin(Long mainAdminId, String shopName) {
        String title = "New Shop Registered \uD83C\uDFEA";
        String body = String.format("'%s' is now live on FadeBook.", shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "shopName", shopName));
    }

    public void sendBarberRegisteredToMainAdmin(Long mainAdminId, String barberName, String shopName) {
        String title = "New Barber Registered \u2702\uFE0F";
        String body = String.format("%s is now active at %s.", barberName, shopName);
        sendPushNotification(mainAdminId, "ADMIN", title, body, Map.of("type", "GENERAL", "barberName", barberName));
    }
}

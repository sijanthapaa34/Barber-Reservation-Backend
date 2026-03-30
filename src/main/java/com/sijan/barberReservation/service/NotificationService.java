package com.sijan.barberReservation.service;

import com.google.firebase.messaging.*;
import com.sijan.barberReservation.model.NotificationToken;
import com.sijan.barberReservation.repository.NotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationTokenRepository tokenRepository;

    public void saveToken(Long userId, String token, String userType) {
        // Avoid duplicates
        tokenRepository.findByToken(token).orElseGet(() -> {
            NotificationToken newToken = new NotificationToken();
            newToken.setUserId(userId);
            newToken.setToken(token);
            newToken.setUserType(userType);
            return tokenRepository.save(newToken);
        });
    }

    public void sendPushNotification(Long userId, String userType, String title, String body) {
        List<NotificationToken> tokens = tokenRepository.findByUserIdAndUserType(userId, userType);

        if (tokens.isEmpty()) {
            log.info("No device tokens found for user: {}", userId);
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .addAllTokens(tokens.stream().map(NotificationToken::getToken).toList())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("Successfully sent messages: {}", response.getSuccessCount());
        } catch (FirebaseMessagingException e) {
            log.error("Error sending FCM", e);
        }
    }
}
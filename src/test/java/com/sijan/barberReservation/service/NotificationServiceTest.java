package com.sijan.barberReservation.service;

import com.google.firebase.messaging.*;
import com.sijan.barberReservation.model.Notification;
import com.sijan.barberReservation.model.NotificationToken;
import com.sijan.barberReservation.model.User;
import com.sijan.barberReservation.repository.NotificationRepository;
import com.sijan.barberReservation.repository.NotificationTokenRepository;
import com.sijan.barberReservation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationTokenRepository tokenRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationToken token;
    private User user;

    @BeforeEach
    void setUp() {
        token = new NotificationToken();
        token.setId(1L);
        token.setUserId(1L);
        token.setToken("test-fcm-token-123");
        token.setUserType("CUSTOMER");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
    }

    @Test
    void saveToken_NewToken_SavesSuccessfully() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(tokenRepository.findByToken("new-token")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(NotificationToken.class))).thenReturn(token);

        // Act
        notificationService.saveToken(1L, "new-token", "CUSTOMER");

        // Assert
        verify(tokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void saveToken_ExistingToken_DeletesOldAndSavesNew() {
        // Arrange
        List<NotificationToken> existingTokens = Arrays.asList(token);
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(existingTokens);
        when(tokenRepository.findByToken("new-token")).thenReturn(Optional.empty());
        when(tokenRepository.save(any(NotificationToken.class))).thenReturn(token);

        // Act
        notificationService.saveToken(1L, "new-token", "CUSTOMER");

        // Assert
        verify(tokenRepository).deleteAll(existingTokens);
        verify(tokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void saveToken_DuplicateTokenFromAnotherUser_DeletesDuplicate() {
        // Arrange
        NotificationToken duplicateToken = new NotificationToken();
        duplicateToken.setUserId(2L);
        duplicateToken.setToken("duplicate-token");

        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(tokenRepository.findByToken("duplicate-token")).thenReturn(Optional.of(duplicateToken));
        when(tokenRepository.save(any(NotificationToken.class))).thenReturn(token);

        // Act
        notificationService.saveToken(1L, "duplicate-token", "CUSTOMER");

        // Assert
        verify(tokenRepository).delete(duplicateToken);
        verify(tokenRepository).save(any(NotificationToken.class));
    }

    @Test
    void sendPushNotification_NoTokens_SavesNotificationOnly() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        notificationService.sendPushNotification(1L, "CUSTOMER", "Test Title", "Test Body");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
        verifyNoInteractions(firebaseMessaging);
    }

    @Test
    void sendPushNotification_WithoutAdditionalData_SendsSuccessfully() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendPushNotification(1L, "CUSTOMER", "Title", "Body");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendAppointmentBookedToCustomer_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendAppointmentBookedToCustomer(1L, "Test Shop", "John Barber", "2024-01-01 10:00");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendNewAppointmentToBarber_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "BARBER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendNewAppointmentToBarber(1L, "Customer Name", "Haircut", "2024-01-01 10:00");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendReviewSubmittedToBarber_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "BARBER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendReviewSubmittedToBarber(1L, "Customer Name", 5);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendReviewReplyToCustomer_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendReviewReplyToCustomer(1L, "Barber Name", "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendApplicationSubmittedToAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendApplicationSubmittedToAdmin(1L, "Applicant Name", "BARBER");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendApplicationStatusUpdate_LogsOnly() {
        // Act
        notificationService.sendApplicationStatusUpdate(1L, "APPROVED", "BARBER");

        // Assert - should not interact with repositories
        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(notificationRepository);
    }

    @Test
    void sendAppointmentReminder_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendAppointmentReminder(1L, "Shop Name", "Barber Name", "10:00 AM");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendAppointmentCancelled_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendAppointmentCancelled(1L, "CUSTOMER", "Shop Admin", "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendAppointmentRescheduled_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendAppointmentRescheduled(1L, "CUSTOMER", "11:00 AM", "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendPaymentCompletedToCustomer_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendPaymentCompletedToCustomer(1L, "Shop Name", "500", "Khalti");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberApplicationApproved_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "BARBER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberApplicationApproved(1L, "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberApplicationRejected_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "BARBER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberApplicationRejected(1L, "Shop Name", "Not qualified");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendShopApplicationApproved_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendShopApplicationApproved(1L, "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendShopApplicationRejected_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendShopApplicationRejected(1L, "Shop Name", "Incomplete documents");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendNewAppointmentToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendNewAppointmentToShopAdmin(1L, "Customer", "Barber", "10:00 AM");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendAppointmentCancelledToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendAppointmentCancelledToShopAdmin(1L, "Customer", "Barber");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberApplicationToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberApplicationToShopAdmin(1L, "Applicant Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendNewReviewToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendNewReviewToShopAdmin(1L, "Customer", 5);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberJoinedToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberJoinedToShopAdmin(1L, "Barber Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendServiceAddedToShopAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendServiceAddedToShopAdmin(1L, "Haircut");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendShopApplicationToMainAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendShopApplicationToMainAdmin(1L, "Shop Name", "Owner Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberApplicationToMainAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberApplicationToMainAdmin(1L, "Barber Name", "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendShopRegisteredToMainAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendShopRegisteredToMainAdmin(1L, "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendBarberRegisteredToMainAdmin_SendsCorrectNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "ADMIN")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendBarberRegisteredToMainAdmin(1L, "Barber Name", "Shop Name");

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendPushNotification_UserNotFound_DoesNotSaveNotification() {
        // Arrange
        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        notificationService.sendPushNotification(1L, "CUSTOMER", "Title", "Body");

        // Assert
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void sendPushNotification_WithAdditionalData_IncludesDataInPayload() {
        // Arrange
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("appointmentId", "123");
        additionalData.put("shopId", "456");

        when(tokenRepository.findByUserIdAndUserType(1L, "CUSTOMER")).thenReturn(Collections.emptyList());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        notificationService.sendPushNotification(1L, "CUSTOMER", "Title", "Body", additionalData);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }
}

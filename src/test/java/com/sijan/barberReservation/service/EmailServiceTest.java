package com.sijan.barberReservation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendHtmlEmail_Success() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "<h1>Test Body</h1>";

        // Act
        emailService.sendHtmlEmail(to, subject, body);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendOtpEmail_Success() {
        // Arrange
        String to = "user@example.com";
        String otp = "123456";

        // Act
        emailService.sendOtpEmail(to, otp);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendRegistrationConfirmation_Success() {
        // Arrange
        String to = "newuser@example.com";
        String userName = "John Doe";

        // Act
        emailService.sendRegistrationConfirmation(to, userName);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_Success() {
        // Arrange
        String to = "user@example.com";
        String userName = "John Doe";
        String resetLink = "https://example.com/reset?token=abc123";

        // Act
        emailService.sendPasswordResetEmail(to, userName, resetLink);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendApplicationSubmissionEmail_Success() {
        // Arrange
        String to = "applicant@example.com";
        String entityName = "Test Barbershop";

        // Act
        emailService.sendApplicationSubmissionEmail(to, entityName);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendApplicationStatusEmail_Approved() {
        // Arrange
        String to = "applicant@example.com";
        String entityName = "Test Barbershop";
        String status = "APPROVED";

        // Act
        emailService.sendApplicationStatusEmail(to, entityName, status);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendApplicationStatusEmail_Rejected() {
        // Arrange
        String to = "applicant@example.com";
        String entityName = "Test Barbershop";
        String status = "REJECTED";

        // Act
        emailService.sendApplicationStatusEmail(to, entityName, status);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAppointmentConfirmationCustomer_Success() {
        // Arrange
        String to = "customer@example.com";
        String customerName = "John Doe";
        String barberName = "Jane Smith";
        String serviceName = "Haircut";
        String date = "2024-12-25";
        String time = "10:00 AM";
        String shopName = "Best Barbers";

        // Act
        emailService.sendAppointmentConfirmationCustomer(to, customerName, barberName, serviceName, date, time, shopName);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendNewBookingAlert_Success() {
        // Arrange
        String to = "barber@example.com";
        String barberName = "Jane Smith";
        String customerName = "John Doe";
        String serviceName = "Haircut";
        String date = "2024-12-25";
        String time = "10:00 AM";

        // Act
        emailService.sendNewBookingAlert(to, barberName, customerName, serviceName, date, time);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAppointmentReminder_Success() {
        // Arrange
        String to = "customer@example.com";
        String customerName = "John Doe";
        String barberName = "Jane Smith";
        String serviceName = "Haircut";
        String time = "10:00 AM";
        String shopName = "Best Barbers";

        // Act
        emailService.sendAppointmentReminder(to, customerName, barberName, serviceName, time, shopName);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAppointmentCancellation_Success() {
        // Arrange
        String to = "customer@example.com";
        String name = "John Doe";
        String serviceName = "Haircut";
        String date = "2024-12-25";
        String cancelledBy = "Barber";

        // Act
        emailService.sendAppointmentCancellation(to, name, serviceName, date, cancelledBy);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendAppointmentReschedule_Success() {
        // Arrange
        String to = "customer@example.com";
        String name = "John Doe";
        String serviceName = "Haircut";
        String oldDate = "2024-12-25";
        String newDate = "2024-12-26";
        String newTime = "11:00 AM";

        // Act
        emailService.sendAppointmentReschedule(to, name, serviceName, oldDate, newDate, newTime);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendNewReviewNotification_Success() {
        // Arrange
        String to = "barber@example.com";
        String barberName = "Jane Smith";
        String customerName = "John Doe";
        int rating = 5;
        String comment = "Excellent service!";

        // Act
        emailService.sendNewReviewNotification(to, barberName, customerName, rating, comment);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPaymentConfirmation_Success() {
        // Arrange
        String to = "customer@example.com";
        String customerName = "John Doe";
        String serviceName = "Haircut";
        String amount = "Rs. 500";
        String date = "2024-12-25";

        // Act
        emailService.sendPaymentConfirmation(to, customerName, serviceName, amount, date);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPayoutConfirmation_Success() {
        // Arrange
        String to = "barber@example.com";
        String userName = "Jane Smith";
        String amount = "Rs. 5000";
        String bankName = "Test Bank";
        String transactionId = "TXN123456";

        // Act
        emailService.sendPayoutConfirmation(to, userName, amount, bankName, transactionId);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendLeaveRequestNotificationAdmin_Success() {
        // Arrange
        String adminEmail = "admin@example.com";
        String barberName = "Jane Smith";
        String startDate = "2024-12-25";
        String endDate = "2024-12-27";
        String reason = "Personal";

        // Act
        emailService.sendLeaveRequestNotificationAdmin(adminEmail, barberName, startDate, endDate, reason);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendLeaveApprovalNotification_Success() {
        // Arrange
        String barberEmail = "barber@example.com";
        String barberName = "Jane Smith";
        String startDate = "2024-12-25";
        String endDate = "2024-12-27";

        // Act
        emailService.sendLeaveApprovalNotification(barberEmail, barberName, startDate, endDate);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendLeaveRejectionNotification_Success() {
        // Arrange
        String barberEmail = "barber@example.com";
        String barberName = "Jane Smith";
        String startDate = "2024-12-25";
        String endDate = "2024-12-27";

        // Act
        emailService.sendLeaveRejectionNotification(barberEmail, barberName, startDate, endDate);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendBarberCredentials_Success() {
        // Arrange
        String barberEmail = "barber@example.com";
        String barberName = "Jane Smith";
        String shopName = "Best Barbers";
        String password = "tempPassword123";

        // Act
        emailService.sendBarberCredentials(barberEmail, barberName, shopName, password);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendShopAdminCredentials_Success() {
        // Arrange
        String adminEmail = "admin@example.com";
        String adminName = "John Admin";
        String shopName = "Best Barbers";
        String password = "tempPassword123";

        // Act
        emailService.sendShopAdminCredentials(adminEmail, adminName, shopName, password);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }
}

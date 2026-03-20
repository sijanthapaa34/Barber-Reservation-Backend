package com.sijan.barberReservation.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    @Qualifier("gmailMailSender")
    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String to, String otp) {
        String subject = "Your Verification Code - Barber Reservation";
        String body = buildOtpTemplate(otp);
        sendHtmlEmail(to, subject, body);
    }

    @Async
    public void sendRegistrationConfirmation(String to, String userName) {
        String subject = "Welcome to Barber Reservation!";
        String body = "<h3>Hello " + userName + ",</h3>" +
                "<p>Thank you for registering with us. Your account is now active.</p>" +
                "<p>Regards,<br>Barber Reservation Team</p>";
        sendHtmlEmail(to, subject, body);
    }

    @Async
    public void sendApplicationSubmissionEmail(String to, String entityName) {
        // entityName will be Shop Name for shops, or Target Shop Name for barbers
        String subject = "Application Submitted - " + entityName;
        String body = "<h3>Hello,</h3>" +
                "<p>Your application for <b>" + entityName + "</b> has been successfully submitted.</p>" +
                "<p>Our admin team will review it shortly. You will receive an email notification once the status changes.</p>" +
                "<p>Regards,<br>Barber Reservation Team</p>";
        sendHtmlEmail(to, subject, body);
    }

    @Async
    public void sendApplicationStatusEmail(String to, String entityName, String status) {
        String subject = "Application Update: " + entityName + " - " + status;

        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<h3>Hello,</h3>");

        if ("APPROVED".equalsIgnoreCase(status)) {
            bodyBuilder.append("<p>Good news! Your application for <b>").append(entityName).append("</b> has been <b>APPROVED</b>.</p>");
            bodyBuilder.append("<p>You can now log in to your dashboard and start setting up your profile.</p>");
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            bodyBuilder.append("<p>We regret to inform you that your application for <b>").append(entityName).append("</b> has been <b>REJECTED</b>.</p>");
            bodyBuilder.append("<p>Please check your application details or contact support for more information.</p>");
        } else {
            // Fallback for other statuses (e.g., PENDING_REVIEW)
            bodyBuilder.append("<p>Your application for <b>").append(entityName).append("</b> status is now: <b>").append(status).append("</b>.</p>");
        }

        bodyBuilder.append("<p>Regards,<br>Barber Reservation Team</p>");

        sendHtmlEmail(to, subject, bodyBuilder.toString());
    }

    private String buildOtpTemplate(String otp) {
        return "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 5px; max-width: 400px; margin: auto;'>" +
                "<h3 style='color: #333;'>Verification Code</h3>" +
                "<p style='font-size: 16px;'>Please use the following One-Time Password (OTP) to verify your email:</p>" +
                "<h2 style='background: #f4f4f4; padding: 10px; text-align: center; letter-spacing: 5px; font-family: monospace;'>" + otp + "</h2>" +
                "<p style='font-size: 12px; color: #888;'>This code will expire in 5 minutes.</p>" +
                "</div>";
    }

    private void sendHtmlEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
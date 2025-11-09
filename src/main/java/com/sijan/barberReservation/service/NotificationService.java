package com.sijan.barberReservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {this.mailSender = mailSender;}


    public void sendAppointmentConfirmation(String to, String customerName, String dateTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Appointment Confirmation");
        message.setText("Dear " + customerName + ",\n\nYour appointment is confirmed for " + dateTime + ".\n\nThank you!");
        mailSender.send(message);
    }

    public void sendReminder(String to, String customerName, String timeLeft) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Appointment Reminder");
        message.setText("Hey " + customerName + ",\n\nJust a reminder — your appointment starts in " + timeLeft + "!");
        mailSender.send(message);
    }

    public void sendWelcomeEmail(String to, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to BarberX!");
        message.setText("Hi " + name + ",\n\nWelcome to BarberX! 🎉\nYour account has been successfully created.\n\nYou can now book appointments easily.\n\nBest,\nThe BarberX Team");
        mailSender.send(message);
    }
}

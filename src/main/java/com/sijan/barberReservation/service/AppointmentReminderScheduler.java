package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.AppointmentStatus;
import com.sijan.barberReservation.repository.AppointmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * Check for appointments that need 45-minute reminders
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void sendAppointmentReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindowStart = now.plusMinutes(40); // 40 minutes from now
        LocalDateTime reminderWindowEnd = now.plusMinutes(50);   // 50 minutes from now

        // Find all scheduled appointments in the 40-50 minute window
        List<Appointment> upcomingAppointments = appointmentRepository
                .findByStatusAndScheduledTimeBetween(
                        AppointmentStatus.SCHEDULED,
                        reminderWindowStart,
                        reminderWindowEnd
                );

        log.info("Found {} appointments needing reminders", upcomingAppointments.size());

        for (Appointment appointment : upcomingAppointments) {
            try {
                sendReminder(appointment);
            } catch (Exception e) {
                log.error("Failed to send reminder for appointment {}: {}", 
                    appointment.getId(), e.getMessage());
            }
        }
    }

    private void sendReminder(Appointment appointment) {
        String customerEmail = appointment.getCustomer().getEmail();
        String customerName = appointment.getCustomer().getName();
        String barberName = appointment.getBarber().getName();
        String shopName = appointment.getBarbershop().getName();
        String serviceNames = appointment.getServices().stream()
                .map(s -> s.getName())
                .collect(Collectors.joining(", "));
        String time = appointment.getScheduledTime().toLocalTime().toString();

        // Send email reminder
        emailService.sendAppointmentReminder(
                customerEmail, 
                customerName, 
                barberName, 
                serviceNames, 
                time, 
                shopName
        );

        // Send push notification
        try {
            notificationService.sendAppointmentReminder(
                    appointment.getCustomer().getId(),
                    shopName,
                    barberName,
                    appointment.getScheduledTime().toString()
            );
        } catch (Exception e) {
            log.warn("Failed to send push notification reminder: {}", e.getMessage());
        }

        log.info("Sent reminder for appointment {} to customer {}", 
            appointment.getId(), customerName);
    }
}

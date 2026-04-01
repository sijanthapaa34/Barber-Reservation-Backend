package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AppointmentRepository;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentBookingServiceImpl implements AppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Appointment bookPaidAppointment(PaymentTransaction tx) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(tx.getCustomer());
        appointment.setBarber(tx.getBarber());
        appointment.setBarbershop(tx.getBarbershop());
        // DO NOT SET SERVICES HERE
        appointment.setScheduledTime(tx.getScheduledTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPaymentStatus(PaymentStatus.PAID);
        appointment.setPaymentMethod(tx.getPaymentMethod());

        try {
            int duration = tx.getServices().stream().mapToInt(ServiceOffering::getDurationMinutes).sum();
            double price = tx.getServices().stream().mapToDouble(s -> s.getPrice().doubleValue()).sum();
            appointment.setTotalDurationMinutes(duration);
            appointment.setTotalPrice(price);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse services during appointment creation");
        }

        try {
            // Must set the transaction reference before first save — payment_transaction_id is NOT NULL
            appointment.setPaymentTransaction(tx);

            Appointment savedAppointment = appointmentRepository.save(appointment);
            entityManager.flush();
            savedAppointment.setServices(new ArrayList<>(tx.getServices()));
            appointmentRepository.save(savedAppointment);
            entityManager.flush();
            tx.setAppointment(savedAppointment);
            transactionRepository.save(tx);

            distributeEarnings(tx, savedAppointment);

            // Send notifications — wrapped in try-catch so failures never affect the booking
            try {
                String scheduledTime = savedAppointment.getScheduledTime().toString();
                Long customerId = savedAppointment.getCustomer().getId();
                Long barberId = savedAppointment.getBarber().getId();
                String shopName = savedAppointment.getBarbershop().getName();
                String barberName = savedAppointment.getBarber().getName();
                String customerName = savedAppointment.getCustomer().getName();
                String serviceName = tx.getServices().stream()
                        .map(ServiceOffering::getName).findFirst().orElse("Service");

                notificationService.sendAppointmentBookedToCustomer(customerId, shopName, barberName, scheduledTime);
                notificationService.sendNewAppointmentToBarber(barberId, customerName, serviceName, scheduledTime);
            } catch (Exception e) {
                log.warn("Failed to send appointment notifications: {}", e.getMessage());
            }

            return savedAppointment;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_barber_scheduled_time")) {
                throw new AppointmentSlotUnavailableException("This slot was just booked by another customer during payment.");
            }
            throw e;
        }
    }

    private void distributeEarnings(PaymentTransaction tx, Appointment appointment) {
        BigDecimal totalAmount = tx.getAmount();
        BigDecimal platformFee = totalAmount.multiply(new BigDecimal("0.05"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal shopEarnings = totalAmount.subtract(platformFee);

        Barbershop shop = appointment.getBarbershop();
        shop.setBalance((shop.getBalance() != null ? shop.getBalance() : BigDecimal.ZERO).add(shopEarnings));

        Barber barber = appointment.getBarber();
        barber.setBalance((barber.getBalance() != null ? barber.getBalance() : BigDecimal.ZERO).add(shopEarnings));

        tx.setPlatformFee(platformFee);
        tx.setShopEarnings(shopEarnings);
    }
}
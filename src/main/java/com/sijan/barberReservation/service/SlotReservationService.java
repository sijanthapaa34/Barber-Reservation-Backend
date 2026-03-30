package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.model.SlotReservation;
import com.sijan.barberReservation.repository.SlotReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotReservationService {

    private final SlotReservationRepository reservationRepository;
    private static final int RESERVATION_TIMEOUT_MINUTES = 10;

    @Transactional
    public void reserveSlot(Long barberId, Long customerId, LocalDateTime scheduledTime, Long paymentTransactionId) {
        // ✅ CHANGED: Delete expired rows to free up the DB Unique Constraint
        reservationRepository.deleteExpiredReservations(LocalDateTime.now());

        if (reservationRepository.existsByBarberIdAndReservedTimeAndStatus(barberId, scheduledTime, SlotReservation.ReservationStatus.ACTIVE)) {
            throw new AppointmentSlotUnavailableException("This slot is currently being booked. Try again shortly.");
        }

        try {
            reservationRepository.save(SlotReservation.builder()
                    .barberId(barberId)
                    .customerId(customerId)
                    .reservedTime(scheduledTime)
                    .paymentTransactionId(paymentTransactionId)
                    .reservedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES))
                    .status(SlotReservation.ReservationStatus.ACTIVE)
                    .build());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new AppointmentSlotUnavailableException("This slot was just taken.");
        }
    }

    @Transactional
    public void consumeReservation(Long paymentTransactionId) {
        reservationRepository.findActiveByTransactionIdWithLock(paymentTransactionId).ifPresent(r -> {
            // Consumed is the ONLY status we keep in the DB permanently (for audit/history)
            r.setStatus(SlotReservation.ReservationStatus.CONSUMED);
            reservationRepository.save(r);
        });
    }

    @Transactional
    public void cancelReservation(Long paymentTransactionId) {
        // ✅ CHANGED: Just delete it immediately! Free up the slot constraint.
        reservationRepository.findActiveByTransactionIdWithLock(paymentTransactionId).ifPresent(reservationRepository::delete);
    }

    @Transactional
    public boolean isReservationActive(Long paymentTransactionId) {
        return reservationRepository.findActiveByTransactionIdWithLock(paymentTransactionId)
                .map(r -> !r.isExpired())
                .orElse(false);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireOldReservations() {
        // ✅ CHANGED: Delete instead of update
        int deleted = reservationRepository.deleteExpiredReservations(LocalDateTime.now());
        if (deleted > 0) log.info("Deleted {} expired slot reservations", deleted);
    }
}
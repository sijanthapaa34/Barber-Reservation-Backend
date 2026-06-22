package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.SlotReservation;
import com.sijan.barberReservation.repository.SlotReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotReservationServiceTest {

    @Mock
    private SlotReservationRepository reservationRepository;

    @InjectMocks
    private SlotReservationService slotReservationService;

    private SlotReservation reservation;
    private LocalDateTime scheduledTime;

    @BeforeEach
    void setUp() {
        scheduledTime = LocalDateTime.now().plusHours(1);

        reservation = SlotReservation.builder()
                .id(1L)
                .barberId(1L)
                .customerId(1L)
                .reservedTime(scheduledTime)
                .paymentTransactionId(100L)
                .reservedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .status(SlotReservation.ReservationStatus.ACTIVE)
                .build();
    }

    @Test
    void reserveSlot_Success() {
        // Arrange
        when(reservationRepository.existsByBarberIdAndReservedTimeAndStatus(
                1L, scheduledTime, SlotReservation.ReservationStatus.ACTIVE))
                .thenReturn(false);
        when(reservationRepository.save(any(SlotReservation.class))).thenReturn(reservation);

        // Act
        slotReservationService.reserveSlot(1L, 1L, scheduledTime, 100L);

        // Assert
        verify(reservationRepository).save(any(SlotReservation.class));
    }

    @Test
    void reserveSlot_SlotAlreadyReserved_ThrowsException() {
        // Arrange
        when(reservationRepository.existsByBarberIdAndReservedTimeAndStatus(
                1L, scheduledTime, SlotReservation.ReservationStatus.ACTIVE))
                .thenReturn(true);

        // Act & Assert
        assertThrows(AppointmentSlotUnavailableException.class, 
                () -> slotReservationService.reserveSlot(1L, 1L, scheduledTime, 100L));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void consumeReservation_Success() {
        // Arrange
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(SlotReservation.class))).thenReturn(reservation);

        // Act
        slotReservationService.consumeReservation(100L);

        // Assert
        verify(reservationRepository).save(argThat(slot -> 
                slot.getStatus() == SlotReservation.ReservationStatus.CONSUMED));
    }

    @Test
    void consumeReservation_NotFound_DoesNothing() {
        // Arrange
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.empty());

        // Act
        slotReservationService.consumeReservation(100L);

        // Assert
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_Success() {
        // Arrange
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.of(reservation));

        // Act
        slotReservationService.cancelReservation(100L);

        // Assert
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void cancelReservation_NotFound_DoesNothing() {
        // Arrange
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.empty());

        // Act
        slotReservationService.cancelReservation(100L);

        // Assert
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void isReservationActive_ActiveAndNotExpired_ReturnsTrue() {
        // Arrange
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.of(reservation));

        // Act
        boolean result = slotReservationService.isReservationActive(100L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isReservationActive_NotFound_ReturnsFalse() {
        // Arrange
        when(reservationRepository.findActiveByTransactionIdWithLock(100L))
                .thenReturn(Optional.empty());

        // Act
        boolean result = slotReservationService.isReservationActive(100L);

        // Assert
        assertFalse(result);
    }

    @Test
    void expireOldReservations_DeletesExpiredReservations() {
        // Arrange
        when(reservationRepository.deleteExpiredReservations(any(LocalDateTime.class)))
                .thenReturn(5);

        // Act
        slotReservationService.expireOldReservations();

        // Assert
        verify(reservationRepository).deleteExpiredReservations(any(LocalDateTime.class));
    }

    @Test
    void findActiveByBarberAndDate_ReturnsActiveTimes() {
        // Arrange
        Barber barber = new Barber();
        barber.setId(1L);
        
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        when(reservationRepository.findByBarberIdAndStatusAndReservedTimeBetween(
                1L, SlotReservation.ReservationStatus.ACTIVE, start, end))
                .thenReturn(List.of(reservation));

        // Act
        List<LocalDateTime> result = slotReservationService.findActiveByBarberAndDate(barber, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(scheduledTime, result.get(0));
    }
}

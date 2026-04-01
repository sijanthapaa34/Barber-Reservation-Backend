package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.SlotReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotReservationRepository extends JpaRepository<SlotReservation, Long> {

    boolean existsByBarberIdAndReservedTimeAndStatus(Long barberId, LocalDateTime reservedTime, SlotReservation.ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sr FROM SlotReservation sr WHERE sr.paymentTransactionId = :txId AND sr.status = 'ACTIVE'")
    Optional<SlotReservation> findActiveByTransactionIdForUpdate(@Param("txId") Long txId);

    default Optional<SlotReservation> findActiveByTransactionIdWithLock(Long txId) {
        return findActiveByTransactionIdForUpdate(txId);
    }

    List<SlotReservation> findByBarberIdAndStatusAndReservedTimeBetween(
            Long barberId,
            SlotReservation.ReservationStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
    @Modifying
    @Query("DELETE FROM SlotReservation sr WHERE sr.status != 'CONSUMED' AND sr.expiresAt < :now")
    int deleteExpiredReservations(@Param("now") LocalDateTime now);
}
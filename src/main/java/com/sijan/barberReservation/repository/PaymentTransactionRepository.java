package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.PaymentTransaction;
import com.sijan.barberReservation.model.TransactionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tx FROM PaymentTransaction tx WHERE tx.id = :id")
    Optional<PaymentTransaction> findByIdWithLock(@Param("id") Long id);

    List<PaymentTransaction> findByStatusAndCreatedAtBefore(
            TransactionStatus status,
            LocalDateTime createdAtBefore
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tx FROM PaymentTransaction tx WHERE tx.pidx = :pidx")
    Optional<PaymentTransaction> findByPidxWithLock(@Param("pidx") String pidx);

    Optional<PaymentTransaction> findByAppointmentId(Long appointmentId);

    List<PaymentTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT COALESCE(SUM(tx.amount), 0) FROM PaymentTransaction tx WHERE tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Double sumRevenueByPaidAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(tx.platformFee), 0) FROM PaymentTransaction tx WHERE tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Double sumPlatformFeeByPaidAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(tx.shopEarnings), 0) FROM PaymentTransaction tx WHERE tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Double sumShopEarningsByPaidAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT tx FROM PaymentTransaction tx WHERE tx.status = 'COMPLETED' ORDER BY tx.paidAt DESC")
    List<PaymentTransaction> findRecentCompleted(org.springframework.data.domain.Pageable pageable);

    // Shop-specific queries
    @Query("SELECT COALESCE(SUM(tx.amount), 0) FROM PaymentTransaction tx WHERE tx.barbershop = :shop AND tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Double sumRevenueByBarbershopAndPaidAtBetween(@Param("shop") com.sijan.barberReservation.model.Barbershop shop, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(tx) FROM PaymentTransaction tx WHERE tx.barbershop = :shop AND tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Integer countByBarbershopAndPaidAtBetween(@Param("shop") com.sijan.barberReservation.model.Barbershop shop, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Barber-specific queries
    @Query("SELECT COALESCE(SUM(tx.amount * 0.57), 0) FROM PaymentTransaction tx WHERE tx.barber = :barber AND tx.status = 'COMPLETED' AND (tx.refundStatus IS NULL OR tx.refundStatus = 'NOT_APPLICABLE' OR tx.refundStatus = 'NOT_REQUIRED') AND tx.paidAt BETWEEN :start AND :end")
    Double sumBarberEarningsByPaidAtBetween(@Param("barber") com.sijan.barberReservation.model.Barber barber, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
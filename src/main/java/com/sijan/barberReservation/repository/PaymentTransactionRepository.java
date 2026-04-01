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
}
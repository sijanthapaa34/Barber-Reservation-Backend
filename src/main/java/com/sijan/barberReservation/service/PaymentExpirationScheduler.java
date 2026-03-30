package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.PaymentTransaction;
import com.sijan.barberReservation.model.TransactionStatus;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import com.sijan.barberReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentExpirationScheduler {

    private final PaymentTransactionRepository transactionRepository;
    private final PaymentService paymentService;

    // Payments expire after 15 minutes without verification
    private static final int PAYMENT_EXPIRATION_MINUTES = 15;

    /**
     * Check for expired pending payments every minute
     */
    @Scheduled(fixedRate = 60000)
    public void expireOldPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(PAYMENT_EXPIRATION_MINUTES);

        List<PaymentTransaction> expiredPayments = transactionRepository
                .findByStatusAndCreatedAtBefore(TransactionStatus.PENDING, cutoff);

        if (!expiredPayments.isEmpty()) {
            log.info("Found {} expired pending payments", expiredPayments.size());

            for (PaymentTransaction tx : expiredPayments) {
                try {
                    paymentService.handleExpiredPayment(tx.getId());
                } catch (Exception e) {
                    log.error("Failed to expire payment: txId={}, error={}", tx.getId(), e.getMessage());
                }
            }
        }
    }
}
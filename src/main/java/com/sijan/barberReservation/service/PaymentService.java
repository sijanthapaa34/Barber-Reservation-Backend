package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final AppointmentBookingService appointmentService;
    private final KhaltiService khaltiService;
    private final EsewaService esewaService;
    private final SlotReservationService slotReservationService;


    // ==================================================================================
    // STEP 1: INITIATE PAYMENT (Unchanged)
    // ==================================================================================
    @Transactional
    public PaymentInitiationResponse initiatePayment(PaymentTransaction transaction, Customer customer) {
        if (transaction.getServices().isEmpty()) {
            throw new ResourceNotFoundException("Services not found");
        }

        BigDecimal totalAmount = transaction.getServices().stream()
                .map(s -> BigDecimal.valueOf(s.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        transaction.setAmount(totalAmount);
        transaction.setCustomer(customer);

        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        try {
            slotReservationService.reserveSlot(
                    savedTransaction.getBarber().getId(),
                    customer.getId(),
                    savedTransaction.getScheduledTime(),
                    savedTransaction.getId()
            );
        } catch (AppointmentSlotUnavailableException e) {
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw e;
        }

        String productName = "Barber Appointment: " + savedTransaction.getBarbershop().getName();

        try {
            if (savedTransaction.getPaymentMethod() == PaymentMethod.KHALTI) {
                Map<String, Object> khaltiRes = khaltiService.initiatePayment(savedTransaction.getId(), totalAmount, productName);
                String paymentUrl = (String) khaltiRes.get("payment_url");
                String pidx = (String) khaltiRes.get("pidx");

                savedTransaction.setPidx(pidx);
                transactionRepository.save(savedTransaction);

                return new PaymentInitiationResponse(savedTransaction.getId(), paymentUrl, "KHALTI", pidx, null);

            } else if (savedTransaction.getPaymentMethod() == PaymentMethod.ESEWA) {
                Map<String, String> esewaData = esewaService.preparePaymentData(savedTransaction.getId(), totalAmount);
                String paymentUrl = esewaData.get("payment_url");
                return new PaymentInitiationResponse(savedTransaction.getId(), paymentUrl, "ESEWA", null, esewaData);

            } else {
                throw new RuntimeException("Unsupported Payment Method");
            }
        } catch (Exception e) {
            slotReservationService.cancelReservation(savedTransaction.getId());
            savedTransaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTransaction);
            throw new RuntimeException("Payment Gateway Error: " + e.getMessage());
        }
    }

    // ==================================================================================
    // STEP 2: VERIFY PAYMENT (Delegates Appointment Creation)
    // ==================================================================================
    @Transactional
    public Appointment verifyAndConfirmPayment(PaymentVerificationRequest request) {
        Long transactionId = request.getTransactionId();

        PaymentTransaction tx = transactionRepository.findByIdWithLock(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (tx.isCompleted() && tx.getAppointment() != null) {
            return tx.getAppointment();
        }
        if (!tx.isPending()) {
            throw new IllegalStateException("Transaction already processed with status: " + tx.getStatus());
        }

        boolean reservationValid = slotReservationService.isReservationActive(transactionId);
        if (!reservationValid) {
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw new RuntimeException("Payment verification timeout: Your reserved slot has expired.");
        }

        boolean isVerified = verifyWithGateway(tx, request);

        if (!isVerified) {
            slotReservationService.cancelReservation(transactionId);
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw new RuntimeException("Payment verification failed with gateway.");
        }

        // 1. Update Payment Transaction status
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setPaidAt(LocalDateTime.now());
        tx.setVerifiedAt(LocalDateTime.now());
        tx.setTransactionId(request.getGatewayTransactionId() != null ? request.getGatewayTransactionId() : request.getRefId());
        tx.setRefId(request.getRefId());
        if (request.getPidx() != null) tx.setPidx(request.getPidx());

        slotReservationService.consumeReservation(transactionId);

        return appointmentService.bookPaidAppointment(tx);
    }

    private boolean verifyWithGateway(PaymentTransaction tx, PaymentVerificationRequest request) {
        try {
            if (tx.getPaymentMethod() == PaymentMethod.KHALTI) {
                if (request.getPidx() == null || request.getPidx().isEmpty()) return false;

                // ✅ RETRY LOGIC FOR KHALTI PENDING STATUS
                int maxRetries = 3;
                for (int i = 0; i < maxRetries; i++) {
                    try {
                        boolean isVerified = khaltiService.verifyPayment(request.getPidx());
                        if (isVerified) {
                            return true; // Payment is "Completed", exit successfully
                        }
                    } catch (Exception e) {
                        log.warn("Khalti lookup attempt {} failed for pidx={}. Reason: {}", i + 1, request.getPidx(), e.getMessage());
                    }

                    // If we reach here, it's either Pending or failed. Wait before retrying.
                    if (i < maxRetries - 1) {
                        try {
                            log.info("Khalti status likely PENDING. Waiting 2 seconds before retry...");
                            Thread.sleep(2000); // Wait 2 seconds
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                }
                return false; // Exhausted all retries

            } else if (tx.getPaymentMethod() == PaymentMethod.ESEWA) {
                if (request.getRefId() == null || request.getRefId().isEmpty()) return false;
                return esewaService.verifyPayment(request.getRefId(), tx.getId(), tx.getAmount());
            }
            return false;
        } catch (Exception e) {
            log.error("Gateway verification error: txId={}", tx.getId(), e);
            return false;
        }
    }

    // ==================================================================================
    // STEP 3: REFUND (Called by AppointmentService)
    // ==================================================================================
    @Transactional
    public void processRefundForAppointment(PaymentTransaction tx, double refundPercentage) {
        // ============ IDEMPOTENCY CHECK ============
        if (tx.getRefundStatus() == RefundStatus.COMPLETED
                || tx.getRefundStatus() == RefundStatus.FAILED_PENDING_REVIEW
                || tx.getRefundStatus() == RefundStatus.NOT_REQUIRED) {
            log.warn("Refund already processed for txId={}, status={}", tx.getId(), tx.getRefundStatus());
            return;
        }

        // ============ VALIDATE TRANSACTION STATE ============
        if (!tx.isCompleted()) {
            throw new IllegalStateException("Cannot refund transaction with status: " + tx.getStatus());
        }

        // ============ CALCULATE AMOUNTS ============
        BigDecimal totalPaid = tx.getAmount();
        BigDecimal refundAmount = totalPaid.multiply(BigDecimal.valueOf(refundPercentage))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal penaltyAmount = totalPaid.subtract(refundAmount);

        log.info("Processing refund for txId={}. Total={}, Refund={}%, RefundAmount={}, Penalty={}",
                tx.getId(), totalPaid, refundPercentage * 100, refundAmount, penaltyAmount);

        // ============ STORE CALCULATED VALUES (regardless of gateway result) ============
        tx.setRefundAmount(refundAmount);
        tx.setRefundPercentage(refundPercentage);
        tx.setPenaltyAmount(penaltyAmount);

        // ============ DETERMINE IF GATEWAY REFUND NEEDED ============
        boolean needsGatewayRefund = refundAmount.compareTo(BigDecimal.ZERO) > 0;
        boolean hasGatewayReference = hasGatewayReferenceForRefund(tx);

        if (needsGatewayRefund && hasGatewayReference) {
            // ============ ATTEMPT GATEWAY REFUND ============
            try {
                boolean gatewaySuccess = callGatewayRefund(tx, refundAmount);

                if (gatewaySuccess) {
                    tx.setRefundStatus(RefundStatus.COMPLETED);
                    tx.setStatus(TransactionStatus.REFUNDED);
                    log.info("Gateway refund successful for txId={}", tx.getId());
                } else {
                    tx.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
                    tx.setStatus(TransactionStatus.REFUNDED); // Keep as refunded for appointment cancellation
                    log.error("Gateway refund returned false for txId={}. Needs manual review.", tx.getId());
                }
            } catch (Exception e) {
                tx.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
                tx.setStatus(TransactionStatus.REFUNDED); // Still mark appointment as cancelled
                log.error("Gateway refund exception for txId={}. Needs manual review.", tx.getId(), e);
            }
        } else if (needsGatewayRefund && !hasGatewayReference) {
            // Refund needed but no gateway reference (shouldn't happen normally)
            tx.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
            tx.setStatus(TransactionStatus.REFUNDED);
            log.error("Refund needed but no gateway reference for txId={}. Needs manual review.", tx.getId());
        } else {
            // No refund needed (full penalty) or zero amount transaction
            tx.setRefundStatus(RefundStatus.NOT_REQUIRED);
            tx.setStatus(TransactionStatus.REFUNDED);
            log.info("No gateway refund required for txId={}, penalty={}", tx.getId(), penaltyAmount);
        }

        transactionRepository.save(tx);
    }

    /**
     * Check if transaction has required reference for gateway refund
     */
    private boolean hasGatewayReferenceForRefund(PaymentTransaction tx) {
        if (tx.getPaymentMethod() == PaymentMethod.KHALTI) {
            return tx.getPidx() != null && !tx.getPidx().isEmpty();
        } else if (tx.getPaymentMethod() == PaymentMethod.ESEWA) {
            return tx.getRefId() != null && !tx.getRefId().isEmpty();
        }
        return false;
    }

    /**
     * Call appropriate gateway for refund
     */
    private boolean callGatewayRefund(PaymentTransaction tx, BigDecimal refundAmount) {
        if (tx.getPaymentMethod() == PaymentMethod.KHALTI) {
            return khaltiService.refundPayment(tx.getPidx(), refundAmount);
        } else if (tx.getPaymentMethod() == PaymentMethod.ESEWA) {
            // eSewa V2 does not provide a programmatic refund API in sandbox/production
            // Refunds must be initiated manually from the eSewa merchant dashboard
            log.warn("eSewa refund for txId={} refId={} amount={} must be processed manually via eSewa merchant dashboard",
                    tx.getId(), tx.getRefId(), refundAmount);
            return false; // Will be marked FAILED_PENDING_REVIEW for manual processing
        }
        return false;
    }

    public void failTransaction(Long transactionId) {
        transactionRepository.findById(transactionId).ifPresent(tx -> {
            if (tx.isPending()) {
                tx.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(tx);
            }
        });
    }

    @Transactional
    public void cancelPayment(Long transactionId) {
        slotReservationService.cancelReservation(transactionId);
        failTransaction(transactionId);
    }

    @Transactional
    public void handleExpiredPayment(Long transactionId) {
        failTransaction(transactionId);
        slotReservationService.cancelReservation(transactionId);
    }

}
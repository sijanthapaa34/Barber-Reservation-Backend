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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository transactionRepository;
    private final AppointmentRepository appointmentRepository;
    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final CustomerService customerService;
    private final ServiceOfferingService serviceService;
    private final AppointmentService appointmentService;
    private final EmailService emailService;
    private final KhaltiService khaltiService;
    private final EsewaService esewaService;
    private final SlotReservationService slotReservationService;

    private static final BigDecimal PLATFORM_FEE_PERCENT = new BigDecimal("0.05");

    // ==================================================================================
    // STEP 1: INITIATE PAYMENT
    // ==================================================================================
    @Transactional
    public PaymentInitiationResponse initiatePayment(Appointment tempAppointment, Customer customer) {
        if (tempAppointment.getServices().isEmpty()) {
            throw new ResourceNotFoundException("Services not found");
        }

        // Calculate Price
        BigDecimal totalAmount = tempAppointment.getServices().stream()
                .map(s -> BigDecimal.valueOf(s.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create Payment Transaction
        PaymentTransaction tx = new PaymentTransaction();
        tx.setStatus(TransactionStatus.PENDING);
        tx.setPaymentMethod(tempAppointment.getPaymentMethod());
        tx.setAmount(totalAmount);
        tx.setCustomer(customer);
        tx.setBarber(tempAppointment.getBarber());
        tx.setBarbershop(tempAppointment.getBarbershop());
        tx.setScheduledTime(tempAppointment.getScheduledTime());
        tx.setServices(tempAppointment.getServices());

        tx = transactionRepository.save(tx);
        log.info("Payment transaction created: id={}, amount={}, method={}",
                tx.getId(), totalAmount, tempAppointment.getPaymentMethod());

        // ✅ RESERVE THE SLOT (prevents race condition)
        try {
            slotReservationService.reserveSlot(
                    tempAppointment.getBarber().getId(),
                    customer.getId(),
                    tempAppointment.getScheduledTime(),
                    tx.getId()
            );
        } catch (AppointmentSlotUnavailableException e) {
            // Slot reservation failed - mark transaction as failed
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw e;
        }

        // Initiate with Payment Gateway
        String productName = "Barber Appointment: " + tempAppointment.getBarbershop().getName();

        try {
            if (tempAppointment.getPaymentMethod() == PaymentMethod.KHALTI) {
                Map<String, Object> khaltiRes = khaltiService.initiatePayment(
                        tx.getId(), totalAmount, productName
                );
                String paymentUrl = (String) khaltiRes.get("payment_url");
                String pidx = (String) khaltiRes.get("pidx");

                tx.setPidx(pidx);
                transactionRepository.save(tx);

                log.info("Khalti payment initiated: txId={}, pidx={}", tx.getId(), pidx);
                return new PaymentInitiationResponse(tx.getId(), paymentUrl, "KHALTI", null);

            } else if (tempAppointment.getPaymentMethod() == PaymentMethod.ESEWA) {
                Map<String, String> esewaData = esewaService.preparePaymentData(tx.getId(), totalAmount);
                String paymentUrl = esewaData.get("payment_url");

                log.info("eSewa payment initiated: txId={}", tx.getId());
                return new PaymentInitiationResponse(tx.getId(), paymentUrl, "ESEWA", esewaData);

            } else {
                throw new RuntimeException("Unsupported Payment Method");
            }
        } catch (Exception e) {
            // Gateway initiation failed - cancel reservation and mark transaction failed
            log.error("Payment gateway initiation failed: txId={}, error={}", tx.getId(), e.getMessage());

            slotReservationService.cancelReservation(tx.getId());

            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);

            throw new RuntimeException("Payment Gateway Error: " + e.getMessage());
        }
    }

    // ==================================================================================
    // STEP 2: VERIFY PAYMENT (IDEMPOTENT + RACE CONDITION SAFE)
    // ==================================================================================
    @Transactional
    public Appointment verifyAndConfirmPayment(PaymentVerificationRequest request) {
        Long transactionId = request.getTransactionId();

        log.info("Payment verification requested: txId={}, pidx={}, refId={}",
                transactionId, request.getPidx(), request.getRefId());

        // ✅ PESSIMISTIC LOCK: Lock the transaction row to prevent concurrent verification
        PaymentTransaction tx = transactionRepository.findByIdWithLock(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        // ✅ IDEMPOTENCY CHECK: If already completed, return existing appointment
        if (tx.isCompleted() && tx.getAppointment() != null) {
            log.info("Transaction already verified, returning existing appointment: txId={}, appointmentId={}",
                    transactionId, tx.getAppointment().getId());
            return tx.getAppointment();
        }

        // ✅ STATUS CHECK: Only process PENDING transactions
        if (!tx.isPending()) {
            log.warn("Transaction not in PENDING state: txId={}, status={}", transactionId, tx.getStatus());
            throw new IllegalStateException(
                    "Transaction already processed with status: " + tx.getStatus()
            );
        }

        // ✅ RESERVATION CHECK: Verify slot reservation is still valid
        boolean reservationValid = slotReservationService.isReservationActive(transactionId);
        if (!reservationValid) {
            log.warn("Slot reservation expired or not found: txId={}", transactionId);
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);
            throw new RuntimeException(
                    "Payment verification timeout: Your reserved slot has expired. Please try booking again."
            );
        }

        // Verify with Payment Gateway
        boolean isVerified = verifyWithGateway(tx, request);

        if (!isVerified) {
            log.warn("Payment verification failed with gateway: txId={}", transactionId);

            // Cancel reservation
            slotReservationService.cancelReservation(transactionId);

            // Mark transaction as failed
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);

            throw new RuntimeException("Payment verification failed with gateway.");
        }

        // ✅ CREATE APPOINTMENT with unique constraint protection
        Appointment appointment;
        try {
            appointment = createAppointmentFromTransaction(tx);
            log.info("Appointment created: appointmentId={} for txId={}", appointment.getId(), transactionId);
        } catch (DataIntegrityViolationException e) {
            // Unique constraint violation - slot was taken by another transaction
            log.error("Unique constraint violation when creating appointment: txId={}", transactionId);

            // Cancel reservation
            slotReservationService.cancelReservation(transactionId);

            // Mark transaction as failed
            tx.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(tx);

            throw new AppointmentSlotUnavailableException(
                    "This slot was taken by another customer during payment. Your payment will be refunded."
            );
        }

        // ✅ UPDATE TRANSACTION
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setPaidAt(LocalDateTime.now());
        tx.setVerifiedAt(LocalDateTime.now());
        tx.setAppointment(appointment);
        tx.setTransactionId(request.getGatewayTransactionId() != null
                ? request.getGatewayTransactionId()
                : request.getRefId());
        tx.setRefId(request.getRefId());

        if (request.getPidx() != null) {
            tx.setPidx(request.getPidx());
        }

        // ✅ CONSUME RESERVATION
        slotReservationService.consumeReservation(transactionId);

        // ✅ DISTRIBUTE EARNINGS
        distributeEarnings(tx, appointment);

        transactionRepository.save(tx);
        log.info("Payment completed successfully: txId={}, appointmentId={}", transactionId, appointment.getId());

        // Send confirmation emails (async, don't fail transaction if email fails)
        sendConfirmationEmailsAsync(appointment);

        return appointment;
    }

    /**
     * Verify payment with the appropriate gateway
     */
    private boolean verifyWithGateway(PaymentTransaction tx, PaymentVerificationRequest request) {
        try {
            if (tx.getPaymentMethod() == PaymentMethod.KHALTI) {
                if (request.getPidx() == null || request.getPidx().isEmpty()) {
                    log.error("Missing pidx for Khalti verification: txId={}", tx.getId());
                    return false;
                }
                return khaltiService.verifyPayment(request.getPidx());

            } else if (tx.getPaymentMethod() == PaymentMethod.ESEWA) {
                if (request.getRefId() == null || request.getRefId().isEmpty()) {
                    log.error("Missing refId for eSewa verification: txId={}", tx.getId());
                    return false;
                }
                return esewaService.verifyPayment(request.getRefId(), tx.getId(), tx.getAmount());

            } else {
                log.error("Unknown payment method: txId={}, method={}", tx.getId(), tx.getPaymentMethod());
                return false;
            }
        } catch (Exception e) {
            log.error("Gateway verification error: txId={}, error={}", tx.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Create appointment from transaction
     */
    private Appointment createAppointmentFromTransaction(PaymentTransaction tx) {
        Appointment appointment = new Appointment();
        appointment.setCustomer(tx.getCustomer());
        appointment.setBarber(tx.getBarber());
        appointment.setBarbershop(tx.getBarbershop());
        appointment.setServices(tx.getServices());

        try {
            int duration = tx.getServices().stream()
                    .mapToInt(ServiceOffering::getDurationMinutes)
                    .sum();
            double price = tx.getServices().stream()
                    .mapToDouble(s -> s.getPrice().doubleValue())
                    .sum();
            appointment.setTotalDurationMinutes(duration);
            appointment.setTotalPrice(price);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse services during appointment creation");
        }

        appointment.setScheduledTime(tx.getScheduledTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPaymentStatus(PaymentStatus.PAID);
        appointment.setPaymentMethod(tx.getPaymentMethod());

        return appointmentRepository.save(appointment);
    }

    /**
     * Distribute earnings between platform and shop
     */
    private void distributeEarnings(PaymentTransaction tx, Appointment appointment) {
        BigDecimal totalAmount = tx.getAmount();
        BigDecimal platformFee = totalAmount.multiply(PLATFORM_FEE_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal shopEarnings = totalAmount.subtract(platformFee);

        Barbershop shop = appointment.getBarbershop();
        shop.setBalance(shop.getBalance().add(shopEarnings));

        Barber barber = appointment.getBarber();
        barber.setBalance(barber.getBalance().add(shopEarnings));

        tx.setPlatformFee(platformFee);
        tx.setShopEarnings(shopEarnings);

        log.info("Earnings distributed: txId={}, total={}, platform={}, shop={}",
                tx.getId(), totalAmount, platformFee, shopEarnings);
    }

    /**
     * Send confirmation emails asynchronously
     */
    private void sendConfirmationEmailsAsync(Appointment appointment) {
        try {
            // Run in separate thread to not block transaction
            Thread.startVirtualThread(() -> {
                try {
                    String customerEmail = appointment.getCustomer().getEmail();
                    String customerName = appointment.getCustomer().getName();
                    String barberName = appointment.getBarber().getName();
                    String shopName = appointment.getBarbershop().getName();
                    String serviceNames = appointment.getServices().stream()
                            .map(ServiceOffering::getName)
                            .collect(java.util.stream.Collectors.joining(", "));
                    String date = appointment.getScheduledTime().toLocalDate().toString();
                    String time = appointment.getScheduledTime().toLocalTime().toString();

                    emailService.sendAppointmentConfirmationCustomer(
                            customerEmail, customerName, barberName, serviceNames, date, time, shopName
                    );
                } catch (Exception e) {
                    log.error("Failed to send confirmation email: appointmentId={}, error={}",
                            appointment.getId(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to start email thread: {}", e.getMessage());
        }
    }

    /**
     * Handle payment timeout (called by scheduled job)
     */
    @Transactional
    public void handleExpiredPayment(Long transactionId) {
        transactionRepository.findById(transactionId).ifPresent(tx -> {
            if (tx.isPending()) {
                log.info("Expiring pending payment: txId={}", transactionId);
                slotReservationService.cancelReservation(transactionId);
                tx.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(tx);
            }
        });
    }

    public BigDecimal getMyBalance(User user) {
        return BigDecimal.valueOf(2);
    }
}
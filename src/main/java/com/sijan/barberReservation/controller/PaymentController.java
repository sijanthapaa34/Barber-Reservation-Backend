package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.mapper.appointment.PaymentMapper;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final CustomerService customerService;
    private final PaymentMapper paymentMapper;
    private final PaymentTransactionRepository transactionRepository;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @RequestBody PaymentRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long customerId = userPrincipal.getId();
        PaymentTransaction transaction = paymentMapper.toEntity(request);
        PaymentInitiationResponse tx = paymentService.initiatePayment(transaction, customerService.findById(customerId));
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/verify")
    public ResponseEntity<AppointmentDetailsResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        // ✅ DEBUG: Log received request
        log.info("=== Received verifyPayment request ===");
        log.info("transactionId: {}", request.getTransactionId());
        log.info("pidx: {}", request.getPidx());
        log.info("refId: {}", request.getRefId());
        log.info("gatewayTransactionId: {}", request.getGatewayTransactionId());
        log.info("=====================================");
        
        Appointment appointment = paymentService.verifyAndConfirmPayment(request);
        return ResponseEntity.ok(appointmentDetailsMapper.toDTO(appointment));
    }

    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long transactionId) {
        paymentService.cancelPayment(transactionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{transactionId}/retry-refund")
    public ResponseEntity<Void> retryRefund(@PathVariable Long transactionId) {
        paymentService.retryRefund(transactionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long customerId = userPrincipal.getId();
        List<PaymentTransaction> txList = transactionRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);

        // Only include completed/refunded transactions
        List<Map<String, Object>> transactions = txList.stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.COMPLETED
                        || tx.getStatus() == TransactionStatus.REFUNDED)
                .map(tx -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", tx.getId());
                    item.put("amount", tx.getAmount());
                    item.put("status", tx.getStatus().name());
                    item.put("refundStatus", tx.getRefundStatus() != null ? tx.getRefundStatus().name() : null);
                    item.put("refundAmount", tx.getRefundAmount());
                    item.put("paymentMethod", tx.getPaymentMethod().name());
                    item.put("createdAt", tx.getCreatedAt());
                    item.put("paidAt", tx.getPaidAt());
                    // Shop name from barbershop
                    if (tx.getBarbershop() != null) {
                        item.put("shopName", tx.getBarbershop().getName());
                    }
                    // Service names from appointment
                    if (tx.getAppointment() != null && tx.getAppointment().getServices() != null) {
                        String services = tx.getAppointment().getServices().stream()
                                .map(ServiceOffering::getName)
                                .collect(Collectors.joining(", "));
                        item.put("services", services);
                    }
                    return item;
                })
                .collect(Collectors.toList());

        // Summary stats
        BigDecimal totalSpent = txList.stream()
                .filter(tx -> tx.getStatus() == TransactionStatus.COMPLETED
                        || tx.getStatus() == TransactionStatus.REFUNDED)
                .map(PaymentTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunded = txList.stream()
                .filter(tx -> tx.getRefundAmount() != null
                        && tx.getRefundStatus() == RefundStatus.COMPLETED)
                .map(PaymentTransaction::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("transactions", transactions);
        result.put("totalSpent", totalSpent);
        result.put("totalRefunded", totalRefunded);
        result.put("transactionCount", transactions.size());
        return ResponseEntity.ok(result);
    }
}
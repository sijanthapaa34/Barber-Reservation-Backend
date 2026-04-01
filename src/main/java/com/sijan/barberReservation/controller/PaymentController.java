package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.mapper.appointment.PaymentMapper;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.PaymentTransaction;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final CustomerService customerService;
    private final PaymentMapper paymentMapper;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @RequestBody PaymentRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long customerId = userPrincipal.getId();
        PaymentTransaction transaction = paymentMapper.toEntity(request);
        PaymentInitiationResponse tx = paymentService.initiatePayment(transaction,customerService.findById(customerId));
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/verify")
    public ResponseEntity<AppointmentDetailsResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        Appointment appointment = paymentService.verifyAndConfirmPayment(request);
        return ResponseEntity.ok(appointmentDetailsMapper.toDTO(appointment));
    }

    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<Void> cancelPayment(@PathVariable Long transactionId) {
        paymentService.cancelPayment(transactionId);
        return ResponseEntity.ok().build();
    }
}
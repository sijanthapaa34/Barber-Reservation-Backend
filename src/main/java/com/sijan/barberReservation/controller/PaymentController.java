package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.CreateAppointmentMapper;
import com.sijan.barberReservation.model.Appointment;
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
    private final CreateAppointmentMapper createAppointmentMapper;
    private final CustomerService customerService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @RequestBody PaymentRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long customerId = userPrincipal.getId();
        Appointment appointment = createAppointmentMapper.toAppointment(request);
        PaymentInitiationResponse tx = paymentService.initiatePayment(appointment,customerService.findById(customerId));
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/verify")
    public ResponseEntity<AppointmentDetailsResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        Appointment appointment = paymentService.verifyAndConfirmPayment(request);
        return ResponseEntity.ok(appointmentDetailsMapper.toDTO(appointment));
    }
}
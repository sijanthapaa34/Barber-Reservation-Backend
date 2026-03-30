package com.sijan.barberReservation.DTO.appointment;

import com.sijan.barberReservation.model.PaymentMethod;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// Request when user clicks "Pay Now"
@Data
public class PaymentInitiationRequest {
    private Long barberId;
    private Long shopId;
    private LocalDateTime scheduledTime;
    private List<Long> serviceIds;
    private PaymentMethod paymentMethod; // KHALTI or ESEWA
}

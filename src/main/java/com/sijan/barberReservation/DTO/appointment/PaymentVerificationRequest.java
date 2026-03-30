package com.sijan.barberReservation.DTO.appointment;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private Long transactionId; // Our internal DB ID

    // Khalti specific
    private String pidx;
    private String gatewayTransactionId; // Khalti idx

    // Esewa specific
    private String refId; // Esewa refId
    private Double totalAmount; // Used for Esewa validation
}

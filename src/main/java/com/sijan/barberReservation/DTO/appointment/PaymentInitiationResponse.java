package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;

// Response returned to Frontend after initiation (contains URL to redirect to)
@Data
@AllArgsConstructor
public class PaymentInitiationResponse {
    private Long transactionId;
    private String paymentUrl; // URL to open in WebView/Browser
    private String paymentMethod; // "KHALTI" or "ESEWA"
    private String pidx;
    private java.util.Map<String, String> formData;
}
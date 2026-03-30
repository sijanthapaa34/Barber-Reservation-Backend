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

    // For Esewa, we might need to return form data instead of just a URL
    // but for modern APIs, usually a URL or parameters are enough.
    private java.util.Map<String, String> formData;
}
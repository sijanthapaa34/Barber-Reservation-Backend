package com.sijan.barberReservation.model;

public enum PaymentStatus {
    PENDING,   // Default: Awaiting payment
    PAID,      // Success: Payment received
    REFUNDED   // Closed: Money returned (fully or partially)
}

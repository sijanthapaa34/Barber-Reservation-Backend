package com.sijan.barberReservation.exception.appointment;

public class AppointmentSlotUnavailableException extends RuntimeException{
    public AppointmentSlotUnavailableException(String message) {
        super(message);
    }
}

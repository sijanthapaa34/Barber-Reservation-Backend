package com.sijan.barberReservation.exception.appointment;

public class InvalidAppointmentTimeException extends RuntimeException{
    public InvalidAppointmentTimeException(String message) {
        super(message);
    }
}

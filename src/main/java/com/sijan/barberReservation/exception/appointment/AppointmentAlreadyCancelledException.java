package com.sijan.barberReservation.exception.appointment;

public class AppointmentAlreadyCancelledException extends RuntimeException {
    public AppointmentAlreadyCancelledException(Long id) {
        super("Appointment already cancelled with id: " + id);
    }
}

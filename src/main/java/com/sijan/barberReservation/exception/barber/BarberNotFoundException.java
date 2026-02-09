package com.sijan.barberReservation.exception.barber;

public class BarberNotFoundException extends RuntimeException {
    public BarberNotFoundException(Long id) {
        super("Barber not found with id: " + id);
    }
}
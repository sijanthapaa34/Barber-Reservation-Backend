package com.sijan.barberReservation.exception.barbershop;

public class BarbershopNotFoundException extends RuntimeException {
    public BarbershopNotFoundException(Long id) {
        super("Barbershop not found with id: " + id);
    }
}
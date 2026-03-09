package com.sijan.barberReservation.exception.admin;

public class AdminNotFoundException extends RuntimeException {
    public AdminNotFoundException(Long id) {
        super("Admin not found with id: " + id);
    }
}
package com.sijan.barberReservation.exception.service;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(Long id) {
        super("Service offering not found with id: " + id);
    }
}
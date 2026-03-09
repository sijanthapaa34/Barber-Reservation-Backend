package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.application.ApplicationNotFoundException;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(()-> new ApplicationNotFoundException(id));
    }
}

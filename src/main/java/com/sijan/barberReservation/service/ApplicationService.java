package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.application.ApplicationNotFoundException;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.model.ApplicationStatus;
import com.sijan.barberReservation.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;

    public ApplicationService(ApplicationRepository applicationRepository, PasswordEncoder passwordEncoder) {
        this.applicationRepository = applicationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(()-> new ApplicationNotFoundException(id));
    }

    public Application save(Application application) {
        String rawPassword = application.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        application.setPassword(encodedPassword);
        return applicationRepository.save(application);
    }

    public Page<Application> getAll(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    @Transactional
    public void approve(Application application) {
        application.setStatus(ApplicationStatus.APPROVED);
    }

    @Transactional
    public void reject(Application application) {
        application.setStatus(ApplicationStatus.REJECTED);
    }
}

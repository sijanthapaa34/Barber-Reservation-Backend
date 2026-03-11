package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.application.ApplicationNotFoundException;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.ApplicationRepository;
import com.sijan.barberReservation.repository.BarbershopRepository;
import com.sijan.barberReservation.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationMapper applicationMapper; // Injecting the mapper

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(()-> new ApplicationNotFoundException(id));
    }

    public Application save(Application application) {
        // Encode password before saving application
        application.setPassword(passwordEncoder.encode(application.getPassword()));

        // Set initial status
        if(application.getType() == ApplicationType.BARBER) {
            application.setStatus(ApplicationStatus.PENDING);
        } else {
            application.setStatus(ApplicationStatus.PENDING);
        }

        return applicationRepository.save(application);
    }

    public Page<Application> getRelevantForMainAdmin(Pageable pageable) {
        // Custom query: Shops that are PENDING, Barbers that are PENDING_MAIN_APPROVAL
        return applicationRepository.findRelevantForMainAdmin(pageable);
    }

    public Page<Application> getPendingForShopAdmin(Long barbershopId, Pageable pageable) {
        // Find barbers applying to specific shop with status PENDING
        return applicationRepository.findByBarbershopIdAndStatus(barbershopId, ApplicationStatus.PENDING, pageable);
    }

    public Page<Application> getAll(Pageable pageable) {
        return applicationRepository.findAll(pageable);
    }

    @Transactional
    public void approveByShopAdmin(Long applicationId) {
        Application app = findById(applicationId);

        if (app.getType() != ApplicationType.BARBER) {
            throw new AccessDeniedException("Shop Admin can only approve Barber applications.");
        }
        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalStateException("Application is not in PENDING state.");
        }

        app.setStatus(ApplicationStatus.PENDING_MAIN_APPROVAL);
        applicationRepository.save(app);
    }

    @Transactional
    public void approveByMainAdmin(Long applicationId) {
        Application app = findById(applicationId);

        if (app.getType() == ApplicationType.BARBER) {
            if (app.getStatus() != ApplicationStatus.PENDING_MAIN_APPROVAL) {
                throw new IllegalStateException("Barber application must be approved by Shop Admin first.");
            }

            Barber barber = applicationMapper.toBarber(app);
            Barbershop barbershop = barbershopService.findById(app.getBarbershopId());
             userService.registerBarber(barber, barbershop);

        }
        else if (app.getType() == ApplicationType.BARBER_SHOP) {
            if (app.getStatus() != ApplicationStatus.PENDING) {
                throw new IllegalStateException("Shop application state is invalid.");
            }

            Barbershop shop = applicationMapper.toBarbershop(app);
            Admin admin = applicationMapper.toAdmin(app);

            barbershopService.createBarbershop(shop);
            userService.registerAdmin(admin, shop);
        }

        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);
    }

    @Transactional
    public void reject(Application application) {
        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
    }
}
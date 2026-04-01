package com.sijan.barberReservation.service;

import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public Application save(Application application) {
        application.setPassword(passwordEncoder.encode(application.getPassword()));
        application.setStatus(ApplicationStatus.PENDING);
        Application savedApplication = applicationRepository.save(application);
        String entityName;
        if (application.getType() == ApplicationType.BARBER_SHOP) {
            entityName = application.getShopName();
//            User admin = userService.findByRole(Roles.MAIN_ADMIN).stream().findFirst().orElse(null);
//            if (admin != null) {
//                notificationService.sendApplicationSubmittedToAdmin(
//                        admin.getId(),
//                        request.getName(),
//                        request.getType().name()
//                );
//            }
        } else {
            entityName = application.getBarbershopName() != null ? application.getBarbershopName() : "Barber Position";
        }

        emailService.sendApplicationSubmissionEmail(application.getEmail(), entityName);


        return savedApplication;
    }

    public Page<Application> getRelevantForMainAdmin(Pageable pageable) {
        return applicationRepository.findRelevantForMainAdmin(pageable);
    }

    public Page<Application> getPendingForShopAdmin(Long barbershopId, Pageable pageable) {
        return applicationRepository.findByBarbershopIdAndStatus(barbershopId, ApplicationStatus.PENDING, pageable);
    }

    public Page<Application> getAllForShopAdmin(Long barbershopId, Pageable pageable) {
        return applicationRepository.findAllBarberApplicationsByShop(barbershopId, pageable);
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

        // Notify applicant that shop admin approved
        notificationService.sendApplicationStatusUpdate(
                app.getId(), "UNDER_REVIEW", app.getType().name());
    }

    @Transactional
    public void approveByMainAdmin(Long applicationId) {
        Application app = findById(applicationId);
        String entityName = "";
        if (userService.existsByEmail(app.getEmail())) {
            throw new RuntimeException("User with email " + app.getEmail() + " already exists. Please reject this application.");
        }

        if (app.getType() == ApplicationType.BARBER) {
            if (app.getStatus() != ApplicationStatus.PENDING_MAIN_APPROVAL) {
                throw new IllegalStateException("Barber application must be approved by Shop Admin first.");
            }

            Barber barber = applicationMapper.toBarber(app);
            Barbershop barbershop = barbershopService.findById(app.getBarbershopId());
            userService.registerBarberOfApplication(barber, barbershop);

            entityName = app.getBarbershopName();

        }
        else if (app.getType() == ApplicationType.BARBER_SHOP) {
            if (app.getStatus() != ApplicationStatus.PENDING) {
                throw new IllegalStateException("Shop application state is invalid.");
            }

            Barbershop shop = applicationMapper.toBarbershop(app);
            Admin admin = applicationMapper.toAdmin(app);

            barbershopService.createBarbershop(shop);
            userService.registerAdminOfApplication(admin, shop);

            entityName = app.getShopName();
        }

        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // Send Approval Email
        emailService.sendApplicationStatusEmail(app.getEmail(), entityName, "APPROVED");

        // Send push notification
        notificationService.sendApplicationStatusUpdate(app.getId(), "APPROVED", app.getType().name());
    }

    @Transactional
    public void reject(Application application) {
        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);

        // Send Rejection Email
        String name = (application.getType() == ApplicationType.BARBER_SHOP)
                ? application.getShopName()
                : application.getBarbershopName();

        emailService.sendApplicationStatusEmail(application.getEmail(), name, "REJECTED");

        // Send push notification
        notificationService.sendApplicationStatusUpdate(application.getId(), "REJECTED", application.getType().name());
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }
}

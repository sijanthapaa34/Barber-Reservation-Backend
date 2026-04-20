package com.sijan.barberReservation.service;

import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.ApplicationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;
    private final BarbershopService barbershopService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AdminRepository adminRepository;

    public Application save(Application application) {
        application.setPassword(passwordEncoder.encode(application.getPassword()));
        application.setStatus(ApplicationStatus.PENDING);
        Application savedApplication = applicationRepository.save(application);
        String entityName;
        if (application.getType() == ApplicationType.BARBER_SHOP) {
            entityName = application.getShopName();
            // Notify all main admins about new shop application
            try {
                adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN).forEach(admin ->
                    notificationService.sendShopApplicationToMainAdmin(admin.getId(), application.getShopName(), application.getName())
                );
            } catch (Exception e) { log.warn("Failed to notify main admins of shop application: {}", e.getMessage()); }
        } else {
            entityName = application.getBarbershopName() != null ? application.getBarbershopName() : "Barber Position";
            // Notify shop admin about new barber application
            try {
                if (application.getBarbershopId() != null) {
                    Barbershop shop = barbershopService.findById(application.getBarbershopId());
                    adminRepository.findByBarbershop(shop).ifPresent(admin ->
                        notificationService.sendBarberApplicationToShopAdmin(admin.getId(), application.getName())
                    );
                }
            } catch (Exception e) { log.warn("Failed to notify shop admin of barber application: {}", e.getMessage()); }
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
        notificationService.sendApplicationStatusUpdate(app.getId(), "UNDER_REVIEW", app.getType().name());

        // Notify all main admins that barber application needs final approval
        try {
            adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN).forEach(admin ->
                notificationService.sendBarberApplicationToMainAdmin(admin.getId(), app.getName(), app.getBarbershopName())
            );
        } catch (Exception e) { log.warn("Failed to notify main admins of barber approval: {}", e.getMessage()); }
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
            
            // ✅ SEND PUSH NOTIFICATION: Barber application approved
            try {
                notificationService.sendBarberApplicationApproved(barber.getId(), barbershop.getName());
            } catch (Exception e) {
                log.error("Failed to send barber approval notification", e);
            }

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
            
            // ✅ SEND PUSH NOTIFICATION: Shop application approved
            try {
                notificationService.sendShopApplicationApproved(admin.getId(), shop.getName());
            } catch (Exception e) {
                log.error("Failed to send shop approval notification", e);
            }
        }

        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // Send Approval Email
        emailService.sendApplicationStatusEmail(app.getEmail(), entityName, "APPROVED");

        // Notify shop admin when barber joins, notify main admins when shop is created
        try {
            if (app.getType() == ApplicationType.BARBER) {
                Barbershop shop = barbershopService.findById(app.getBarbershopId());
                adminRepository.findByBarbershop(shop).ifPresent(admin ->
                    notificationService.sendBarberJoinedToShopAdmin(admin.getId(), app.getName())
                );
                adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN).forEach(admin ->
                    notificationService.sendBarberRegisteredToMainAdmin(admin.getId(), app.getName(), app.getBarbershopName())
                );
            } else if (app.getType() == ApplicationType.BARBER_SHOP) {
                adminRepository.findAllByAdminLevel(AdminLevel.SUPER_ADMIN).forEach(admin ->
                    notificationService.sendShopRegisteredToMainAdmin(admin.getId(), app.getShopName())
                );
            }
        } catch (Exception e) { log.warn("Failed to send post-approval notifications: {}", e.getMessage()); }
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

        // ✅ SEND PUSH NOTIFICATION: Application rejected
        // Note: This won't work for new applicants who aren't registered yet
        // But will work if they reapply after being rejected
        try {
            User user = userService.findByEmail(application.getEmail());
            if (user != null) {
                if (application.getType() == ApplicationType.BARBER && user instanceof Barber barber) {
                    notificationService.sendBarberApplicationRejected(
                        barber.getId(), 
                        application.getBarbershopName(), 
                        "Please contact support for more details."
                    );
                } else if (application.getType() == ApplicationType.BARBER_SHOP && user instanceof Admin admin) {
                    notificationService.sendShopApplicationRejected(
                        admin.getId(), 
                        application.getShopName(), 
                        "Please contact support for more details."
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send rejection push notification: {}", e.getMessage());
        }
    }

    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }
}

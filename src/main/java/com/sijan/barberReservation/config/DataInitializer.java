package com.sijan.barberReservation.config;

import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.AdminLevel;
import com.sijan.barberReservation.model.Roles;
import com.sijan.barberReservation.repository.AdminRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeMainAdmin();
    }

    private void initializeMainAdmin() {
        // Check if main admin already exists
        if (adminRepository.existsByAdminLevel(AdminLevel.SUPER_ADMIN)) {
            System.out.println("Main admin already exists!");
            return;
        }

        // Create main admin
        Admin mainAdmin = new Admin();
        mainAdmin.setName("Main Admin");
        mainAdmin.setEmail("admin@barberapp.com");
        mainAdmin.setPassword(passwordEncoder.encode("admin123"));
        mainAdmin.setRole(Roles.MAIN_ADMIN);
        mainAdmin.setAdminLevel(AdminLevel.SUPER_ADMIN);
        mainAdmin.setEmailNotificationsEnabled(true);

        adminRepository.save(mainAdmin);
        System.out.println("✅ Main admin created successfully!");
        System.out.println("   Email: admin@barberapp.com");
        System.out.println("   Password: admin123");
    }
}
package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final BarbershopMapper barbershopMapper;

    public AdminService(AdminRepository adminRepository, BarbershopRepository barbershopRepository, UserRepository userRepository, AppointmentRepository appointmentRepository, BarbershopMapper barbershopMapper) {
        this.adminRepository = adminRepository;
        this.barbershopRepository = barbershopRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.barbershopMapper = barbershopMapper;
    }
    public Admin findById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(()-> new RuntimeException("Admin id not found"));
    }

    public AdminDashboardResponse getDashboardData() {
        // 1. Stats
        long users = userRepository.count();
        long shops = barbershopRepository.countByActiveTrue();

        // Calculate monthly revenue (example using local date)
        LocalDate startOfMonth = YearMonth.now().atDay(1);
//        double monthlyRev = paymentRepository.sumAmountByDateAfter(startOfMonth);

        long bookings = appointmentRepository.count();

        // 2. Revenue Split (You need a Payment/Transaction table)
//        // Example: Sum of all completed payments split by type
//        double barberCut = paymentRepository.sumBarberCommission();
//        double shopCut = paymentRepository.sumShopCommission();
//        double platformCut = paymentRepository.sumPlatformFee();

        // 3. System Health
        // This is often complex. For now, you can return static values or integrate Spring Boot Actuator.
        AdminDashboardResponse.SystemHealth health = new AdminDashboardResponse.SystemHealth();
        health.setUptime("99.98%");
        health.setActiveSessions(userRepository.countByLastLoginAfter(LocalDateTime.now().minusMinutes(15))); // Example logic
        health.setErrorRate("0.02%");

        // 4. Config
        // Ideally store these in a "Settings" table or properties
        AdminDashboardResponse.CommissionConfig config = new AdminDashboardResponse.CommissionConfig();
        config.setPlatformFee(10.0);
        config.setDefaultBarberCut(60.0);
        // ...

        // 5. Top Shops
        List<Barbershop> topShops = barbershopRepository.findTop4ByActiveTrueOrderByRatingDesc();


        // Map to DTO...
        return new AdminDashboardResponse(users, shops, 0.0, bookings, 0.0,0.0,0.0, health, config, barbershopMapper.toDTOs(topShops));
    }
}


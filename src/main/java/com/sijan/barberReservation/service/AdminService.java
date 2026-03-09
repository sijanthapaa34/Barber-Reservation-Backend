package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.exception.admin.AdminNotFoundException;
import com.sijan.barberReservation.exception.auth.InvalidPasswordException;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BarbershopService barbershopService;
    private final BarberService barberService;
    private final ReviewService reviewService;
    private final ServiceOfferingService serviceService;
    private final UserService userService;
    private final AppointmentService appointmentService;
    private final BarbershopMapper barbershopMapper;
    private final BarberMapper barberMapper;
    private final ServiceMapper serviceMapper;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final PasswordEncoder passwordEncoder;


    public AdminService(AdminRepository adminRepository, BarbershopService barbershopService, BarberService barberService, ReviewService reviewService, ServiceOfferingService serviceService, UserService userService, AppointmentService appointmentService, BarbershopMapper barbershopMapper, BarberMapper barberMapper, ServiceMapper serviceMapper, AppointmentDetailsMapper appointmentDetailsMapper, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.barbershopService = barbershopService;
        this.barberService = barberService;
        this.reviewService = reviewService;
        this.serviceService = serviceService;
        this.userService = userService;
        this.appointmentService = appointmentService;
        this.barbershopMapper = barbershopMapper;
        this.barberMapper = barberMapper;
        this.serviceMapper = serviceMapper;
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(()-> new AdminNotFoundException(id));
    }

    public AdminDashboardResponse getDashboardData() {
        // 1. Stats
        long users = userService.count();
        long shops = barbershopService.countByActiveTrue();

        // Calculate monthly revenue (example using local date)
        LocalDate startOfMonth = YearMonth.now().atDay(1);
//        double monthlyRev = paymentRepository.sumAmountByDateAfter(startOfMonth);

        long bookings = appointmentService.count();

        // 2. Revenue Split (You need a Payment/Transaction table)
//        // Example: Sum of all completed payments split by type
//        double barberCut = paymentRepository.sumBarberCommission();
//        double shopCut = paymentRepository.sumShopCommission();
//        double platformCut = paymentRepository.sumPlatformFee();

        // 3. System Health
        // This is often complex. For now, you can return static values or integrate Spring Boot Actuator.
        AdminDashboardResponse.SystemHealth health = new AdminDashboardResponse.SystemHealth();
        health.setUptime("99.98%");
        health.setActiveSessions(userService.countByLastLoginAfter(LocalDateTime.now().minusMinutes(15))); // Example logic
        health.setErrorRate("0.02%");

        // 4. Config
        // Ideally store these in a "Settings" table or properties
        AdminDashboardResponse.CommissionConfig config = new AdminDashboardResponse.CommissionConfig();
        config.setPlatformFee(10.0);
        config.setDefaultBarberCut(60.0);
        // ...

        // 5. Top Shops
        List<Barbershop> topShops = barbershopService.findTop4ByActiveTrueOrderByRatingDesc();


        // Map to DTO...
        return new AdminDashboardResponse(users, shops, 0.0, bookings, 0.0,0.0,0.0, health, config, barbershopMapper.toDTOs(topShops));
    }

    public ShopAdminDashboardResponse getShopAdminDashboardData(Admin admin) {
        Barbershop shop = admin.getBarbershop();
        LocalDate todayDate = LocalDate.now();
        LocalDateTime startOfDay = todayDate.atStartOfDay();
        LocalDateTime endOfDay = todayDate.atTime(LocalTime.MAX);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // 3. Fetch Basic Stats
        Integer totalBarbers = barberService.countByBarbershop(shop);
        Integer totalReviews = reviewService.countByBarbershop(shop); // Assuming ReviewRepository exists

        // 4. Fetch Today's Stats
        Integer todayAppointments = appointmentService.countByShopAndScheduledTimeBetween(shop, startOfDay, endOfDay);
        Double todayRevenue = appointmentService.sumRevenueByShopAndScheduledTimeBetween(shop, startOfDay, endOfDay);
        if (todayRevenue == null) todayRevenue = 0.0;

        Integer pendingAppointments = appointmentService.countByShopAndStatus(shop, AppointmentStatus.IN_PROGRESS); // Assuming enum Status
        Integer availableBarbers = barberService.countByBarbershopAndAvailableTrue(shop);

        // 5. Fetch Monthly Stats
        Integer monthlyAppointments = appointmentService.countByShopAndScheduledTimeBetween(shop, startOfMonth, endOfMonth);
        Double monthlyRevenue = appointmentService.sumRevenueByShopAndScheduledTimeBetween(shop, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) monthlyRevenue = 0.0;

        Double lastMonthRevenue = appointmentService.sumRevenueByShopAndScheduledTimeBetween(shop, startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) lastMonthRevenue = 0.0;

        // Calculate Growth
        Double revenueGrowth = 0.0;
        if (lastMonthRevenue > 0) {
            revenueGrowth = ((monthlyRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
        } else if (monthlyRevenue > 0) {
            revenueGrowth = 100.0; // Infinite growth from 0 is treated as 100%
        }

        // 6. Calculate Splits (Assuming fixed percentages or from config)
        double shopShare = 0.30; // 30%
        double barberShare = 0.60; // 60%
        double platformShare = 0.10; // 10%

        Double shopEarnings = monthlyRevenue * shopShare;
        Double barbersEarnings = monthlyRevenue * barberShare;
        Double platformFees = monthlyRevenue * platformShare;

        // 7. Fetch Lists (Top 5)
        List<Barber> topBarbers = barberService.findTopBarbersByShops(shop, PageRequest.of(0, 5));
        List<ServiceOffering> popularServices = serviceService.findPopularServices(shop, PageRequest.of(0, 5));
        List<Appointment> upcomingAppointments = appointmentService.findUpcomingByShop(shop, LocalDateTime.now(), PageRequest.of(0, 5));

        // 8. Build Response
        return new ShopAdminDashboardResponse(
                totalReviews,
                totalBarbers,
                todayAppointments,
                todayRevenue,
                pendingAppointments,
                availableBarbers,
                monthlyRevenue,
                monthlyAppointments,
                Math.round(revenueGrowth * 100.0) / 100.0, // Round to 2 decimals
                shopEarnings,
                barbersEarnings,
                platformFees,
                barberMapper.toDTOs(topBarbers),
                serviceMapper.toDTOs(popularServices),
                appointmentDetailsMapper.toDTOs(upcomingAppointments)
        );
    }

    @Transactional
    public Admin update(Admin admin, String name, String phone) {
        admin.setName(name);
        admin.setPhone(phone);
        return admin;
    }

    @Transactional
    public void changePassword(Admin admin, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
    }
}


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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    private final PaymentTransactionRepository paymentTransactionRepository;

    public Admin findById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(()-> new AdminNotFoundException(id));
    }

    @Transactional
    public AdminDashboardResponse getDashboardData() {
        // Count all users (customers, barbers, admins)
        long users = userService.count();
        System.out.println("DEBUG: Total Users = " + users);
        
        // Count active barbershops
        long shops = barbershopService.countByActiveTrue();
        System.out.println("DEBUG: Active Shops = " + shops);
        
        // Count total bookings from appointments
        long bookings = appointmentService.count();
        System.out.println("DEBUG: Total Bookings = " + bookings);

        // Monthly revenue from real payment data
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        YearMonth lastMonth = currentMonth.minusMonths(1);
        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);

        // Get monthly revenue from completed payment transactions
        Double monthlyRevenue = paymentTransactionRepository.sumRevenueByPaidAtBetween(startOfMonth, endOfMonth);
        if (monthlyRevenue == null) monthlyRevenue = 0.0;
        System.out.println("DEBUG: Monthly Revenue = " + monthlyRevenue);

        Double lastMonthRevenue = paymentTransactionRepository.sumRevenueByPaidAtBetween(startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) lastMonthRevenue = 0.0;
        System.out.println("DEBUG: Last Month Revenue = " + lastMonthRevenue);

        // Get platform earnings
        Double platformEarnings = paymentTransactionRepository.sumPlatformFeeByPaidAtBetween(startOfMonth, endOfMonth);
        if (platformEarnings == null) platformEarnings = 0.0;
        System.out.println("DEBUG: Platform Earnings = " + platformEarnings);

        // Get total shop earnings (this includes both shop and barber portions)
        Double totalShopEarnings = paymentTransactionRepository.sumShopEarningsByPaidAtBetween(startOfMonth, endOfMonth);
        if (totalShopEarnings == null) totalShopEarnings = 0.0;
        System.out.println("DEBUG: Total Shop Earnings = " + totalShopEarnings);

        // Split shop earnings: 60% to barbers, 40% to shop admins
        double barberEarnings = totalShopEarnings * 0.60;
        double actualShopEarnings = totalShopEarnings * 0.40;
        System.out.println("DEBUG: Barber Earnings = " + barberEarnings);
        System.out.println("DEBUG: Actual Shop Earnings = " + actualShopEarnings);

        // Calculate revenue growth percentage
        double revenueGrowth = 0.0;
        if (lastMonthRevenue > 0) {
            revenueGrowth = ((monthlyRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
        } else if (monthlyRevenue > 0) {
            revenueGrowth = 100.0;
        }
        System.out.println("DEBUG: Revenue Growth = " + revenueGrowth + "%");

        // System Health
        AdminDashboardResponse.SystemHealth health = new AdminDashboardResponse.SystemHealth();
        health.setUptime("99.98%");
        health.setActiveSessions(userService.countByLastLoginAfter(LocalDateTime.now().minusMinutes(15)));
        health.setAvgResponseTime("120ms");
        health.setErrorRate("0.02%");

        // Commission Config
        AdminDashboardResponse.CommissionConfig config = new AdminDashboardResponse.CommissionConfig();
        config.setPlatformFee(5.0);
        config.setDefaultBarberCut(57.0);
        config.setDefaultShopCut(38.0);
        config.setCancellationFee(0.0);

        // Top Shops by rating
        List<Barbershop> topShops = barbershopService.findTop4ByActiveTrueOrderByRatingDesc();
        System.out.println("DEBUG: Top Shops Count = " + topShops.size());

        // Recent Activities — last 10 completed transactions
        List<AdminDashboardResponse.ActivityItem> activities = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, h:mm a");
        try {
            List<PaymentTransaction> recent = paymentTransactionRepository.findRecentCompleted(PageRequest.of(0, 10));
            System.out.println("DEBUG: Recent Activities Count = " + recent.size());
            for (PaymentTransaction tx : recent) {
                String shopName = tx.getBarbershop() != null ? tx.getBarbershop().getName() : "Shop";
                String customerName = tx.getCustomer() != null ? tx.getCustomer().getName() : "Customer";
                String time = tx.getPaidAt() != null ? tx.getPaidAt().format(fmt) : "";
                activities.add(new AdminDashboardResponse.ActivityItem(
                        "BOOKING",
                        "New Booking — " + shopName,
                        customerName + " · Rs. " + tx.getAmount().toPlainString(),
                        time
                ));
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Error fetching recent activities: " + e.getMessage());
            e.printStackTrace();
        }

        AdminDashboardResponse response = new AdminDashboardResponse(
                users, shops, monthlyRevenue, bookings,
                barberEarnings, actualShopEarnings, platformEarnings,
                health, config, barbershopMapper.toDTOs(topShops)
        );
        response.setLastMonthRevenue(lastMonthRevenue);
        response.setRevenueGrowthPercent(Math.round(revenueGrowth * 10.0) / 10.0);
        response.setRecentActivities(activities);
        
        System.out.println("DEBUG: Response created - Users: " + response.getTotalUsers() + 
                          ", Shops: " + response.getActiveShops() + 
                          ", Revenue: " + response.getMonthlyRevenue() + 
                          ", Bookings: " + response.getTotalBookings());
        
        return response;
    }
    @Transactional
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

        // 1. Fetch Basic Stats
        Integer totalBarbers = barberService.countByBarbershop(shop);
        Integer totalReviews = reviewService.countByBarbershop(shop).intValue();

        // 2. Fetch Today's Stats from Payment Transactions
        Integer todayAppointments = paymentTransactionRepository.countByBarbershopAndPaidAtBetween(shop, startOfDay, endOfDay);
        if (todayAppointments == null) todayAppointments = 0;
        
        Double todayRevenue = paymentTransactionRepository.sumRevenueByBarbershopAndPaidAtBetween(shop, startOfDay, endOfDay);
        if (todayRevenue == null) todayRevenue = 0.0;

        // Pending appointments (from appointment table) - using SCHEDULED status
        Integer pendingAppointments = appointmentService.countByShopAndStatus(shop, AppointmentStatus.SCHEDULED);
        if (pendingAppointments == null) pendingAppointments = 0;
        
        // Available barbers
        Integer availableBarbers = barberService.countByBarbershopAndAvailableTrue(shop);
        if (availableBarbers == null) availableBarbers = 0;

        // 3. Fetch Monthly Stats from Payment Transactions
        Integer monthlyAppointments = paymentTransactionRepository.countByBarbershopAndPaidAtBetween(shop, startOfMonth, endOfMonth);
        if (monthlyAppointments == null) monthlyAppointments = 0;
        
        Double monthlyRevenue = paymentTransactionRepository.sumRevenueByBarbershopAndPaidAtBetween(shop, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) monthlyRevenue = 0.0;

        Double lastMonthRevenue = paymentTransactionRepository.sumRevenueByBarbershopAndPaidAtBetween(shop, startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) lastMonthRevenue = 0.0;

        // 4. Calculate Growth
        Double revenueGrowth = 0.0;
        if (lastMonthRevenue > 0) {
            revenueGrowth = ((monthlyRevenue - lastMonthRevenue) / lastMonthRevenue) * 100;
        } else if (monthlyRevenue > 0) {
            revenueGrowth = 100.0; // Infinite growth from 0 is treated as 100%
        }

        // 5. Calculate Revenue Splits (Based on commission structure)
        // Platform takes 5%, remaining 95% is split between shop (40%) and barbers (60%)
        double platformShare = 0.05;  // 5%
        double shopShare = 0.38;       // 38% of total (40% of remaining after platform)
        double barberShare = 0.57;     // 57% of total (60% of remaining after platform)

        Double platformFees = monthlyRevenue * platformShare;
        Double shopEarnings = monthlyRevenue * shopShare;
        Double barbersEarnings = monthlyRevenue * barberShare;

        // 6. Fetch Lists (Top 5)
        List<Barber> topBarbers = barberService.findTopBarbersByShops(shop, PageRequest.of(0, 5));
        List<ServiceOffering> popularServices = serviceService.findPopularServices(shop, PageRequest.of(0, 5));
        List<Appointment> upcomingAppointments = appointmentService.findUpcomingByShop(shop, LocalDateTime.now(), PageRequest.of(0, 5));

        // Convert to maps
        Map<Long, String> topBarbersMap = topBarbers.stream()
                .collect(Collectors.toMap(Barber::getId, User::getName));

        Map<Long, String> popularServicesMap = popularServices.stream()
                .collect(Collectors.toMap(
                        ServiceOffering::getId,
                        s -> s.getName() + " - Rs. " + s.getPrice()
                ));

        // 7. Build Response
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
                topBarbersMap,
                popularServicesMap,
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


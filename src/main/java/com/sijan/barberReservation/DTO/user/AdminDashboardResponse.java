package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long activeShops;
    private double monthlyRevenue;
    private long totalBookings;

    private double barberEarnings;
    private double shopEarnings;
    private double platformEarnings;

    private SystemHealth health;
    private CommissionConfig config;
    private List<BarbershopDTO> topShops;

    // Real revenue data
    private double lastMonthRevenue;
    private double revenueGrowthPercent;

    // Recent activities
    private List<ActivityItem> recentActivities;

    public AdminDashboardResponse(long users, long shops, Double monthlyRevenue, long bookings, double barberEarnings, double actualShopEarnings, Double platformEarnings, SystemHealth health, CommissionConfig config, List<BarbershopDTO> topShops) {
        this.totalUsers = users;
        this.activeShops = shops;
        this.monthlyRevenue = monthlyRevenue != null ? monthlyRevenue : 0.0;
        this.totalBookings = bookings;
        this.barberEarnings = barberEarnings;
        this.shopEarnings = actualShopEarnings;
        this.platformEarnings = platformEarnings != null ? platformEarnings : 0.0;
        this.health = health;
        this.config = config;
        this.topShops = topShops;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemHealth {
        private String uptime;
        private String avgResponseTime;
        private int activeSessions;
        private String errorRate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommissionConfig {
        private double platformFee;
        private double defaultShopCut;
        private double defaultBarberCut;
        private double cancellationFee;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ActivityItem {
        private String type;       // BOOKING, CANCELLATION, NEW_SHOP, NEW_BARBER, NEW_REVIEW
        private String title;
        private String subtitle;
        private String timestamp;
    }
}
package com.sijan.barberReservation.DTO.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardResponse {

    // 1. Stats Grid
    private long totalUsers;
    private long activeShops;
    private double monthlyRevenue;
    private long totalBookings;

    // 2. Revenue Split (Calated from payments)
    private double barberEarnings;
    private double shopEarnings;
    private double platformEarnings;

    // 3. System Health (Can be mocked or pulled from Actuator)
    private SystemHealth health;

    // 4. Config
    private CommissionConfig config;

    // 5. Top Shops (List of simplified ShopDTO)
    private List<BarbershopDTO> topShops;

    // --- Inner Classes ---

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemHealth {
        private String uptime; // e.g., "99.98%"
        private String avgResponseTime; // e.g., "45ms"
        private int activeSessions;
        private String errorRate; // e.g., "0.02%"
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommissionConfig {
        private double platformFee;       // 10.0
        private double defaultShopCut;    // 30.0
        private double defaultBarberCut;  // 60.0
        private double cancellationFee;   // 20.0
    }
}
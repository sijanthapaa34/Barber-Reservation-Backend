package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopAdminDashboardResponse {

    // ==========================================
    // 1. SHOP CONTEXT
    // ==========================================
    private Integer totalReviews;
    private Integer totalBarbers; // Active barbers count

    // ==========================================
    // 2. TODAY'S OVERVIEW
    // ==========================================
    private Integer todayAppointments;     // Total bookings scheduled for today
    private Double todayRevenue;           // Revenue generated today
    private Integer pendingAppointments;   // Appointments awaiting confirmation
    private Integer availableBarbers;      // Barbers currently on shift

    // ==========================================
    // 3. MONTHLY FINANCIALS
    // ==========================================
    private Double monthlyRevenue;         // Total revenue this month
    private Integer monthlyAppointments;   // Total bookings this month
    private Double revenueGrowth;          // % Growth compared to last month (e.g., 15.5)

    // ==========================================
    // 4. REVENUE DISTRIBUTION (Current Month)
    // ==========================================
    private Double shopEarnings;           // Shop's share (e.g., 30%)
    private Double barbersEarnings;        // Barbers' share (e.g., 60%)
    private Double platformFees;           // Platform's share (e.g., 10%)
    // Top performing barbers this month
    private List<BarberDTO> topBarbers;

    // Most booked services this month
    private List<ServiceDTO> popularServices;

    // Upcoming appointments (next 5)
    private List<AppointmentDetailsResponse> upcomingAppointments;
}

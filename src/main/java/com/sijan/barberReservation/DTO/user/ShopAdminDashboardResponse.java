package com.sijan.barberReservation.DTO.user;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopAdminDashboardResponse {
    private Integer totalReviews;
    private Integer totalBarbers;
    private Integer todayAppointments;     // Total bookings scheduled for today
    private Double todayRevenue;           // Revenue generated today
    private Integer pendingAppointments;   // Appointments awaiting confirmation
    private Integer availableBarbers;      // Barbers currently on shift

    private Double monthlyRevenue;         // Total revenue this month
    private Integer monthlyAppointments;   // Total bookings this month
    private Double revenueGrowth;          // % Growth compared to last month (e.g., 15.5)
    private Double shopEarnings;           // Shop's share (e.g., 30%)
    private Double barbersEarnings;        // Barbers' share (e.g., 60%)
    private Double platformFees;           // Platform's share (e.g., 10%)
    // Top performing barbers this month
    private Map<Long,String> topBarbers;

    // Most booked services this month
    private Map<Long,String> popularServices;

    // Upcoming appointments (next 5)
    private List<AppointmentDetailsResponse> upcomingAppointments;
}

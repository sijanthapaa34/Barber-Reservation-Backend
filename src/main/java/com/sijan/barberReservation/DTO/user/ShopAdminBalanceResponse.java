package com.sijan.barberReservation.DTO.user;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ShopAdminBalanceResponse {
    private BigDecimal shopGrossRevenue;
    private BigDecimal totalPlatformFees;
    private BigDecimal shopNetRevenue;
    private BigDecimal pendingPayout;
    private BigDecimal completedPayout;
    private Integer totalAppointments;
    private List<BarberEarningsSummary> barberSummaries;

    @Data
    @Builder
    public static class BarberEarningsSummary {
        private Long barberId;
        private String barberName;
        private BigDecimal grossEarnings;
        private BigDecimal netEarnings;
        private Integer totalAppointments;
    }
}

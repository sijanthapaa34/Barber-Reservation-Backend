package com.sijan.barberReservation.DTO.user;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class BarberBalanceResponse {
    private BigDecimal grossEarnings;
    private BigDecimal platformFees;
    private BigDecimal penaltiesDeducted;
    private BigDecimal netEarnings;
    private BigDecimal pendingPayout;
    private BigDecimal completedPayout;
    private Integer totalAppointments;
    private Integer cancelledAppointments;
}

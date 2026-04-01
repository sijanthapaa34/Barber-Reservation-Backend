package com.sijan.barberReservation.DTO.user;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminBalanceResponse {
    private BigDecimal totalTransactionVolume;
    private BigDecimal totalPlatformFees;
    private BigDecimal totalPenalties;
    private BigDecimal totalRefundsIssued;
    private BigDecimal netPlatformRevenue;
    private Integer totalTransactions;
    private Integer completedTransactions;
    private Integer failedTransactions;
    private Integer refundedTransactions;
}

package com.sijan.barberReservation.DTO.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String reason;
}

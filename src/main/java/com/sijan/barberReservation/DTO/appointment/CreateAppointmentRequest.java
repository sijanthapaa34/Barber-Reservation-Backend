package com.sijan.barberReservation.DTO.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;


import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    @NotNull
    private Long barberId;

    @NotNull
    private Long barbershopId;

    @NotNull
    @Size(min = 1)
    private List<Long> serviceIds;

    @NotNull
    @Future
    private LocalDateTime scheduledTime;

    @Size(max = 500)
    private String customerNotes;
}


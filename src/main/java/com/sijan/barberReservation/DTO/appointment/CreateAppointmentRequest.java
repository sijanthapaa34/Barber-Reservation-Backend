package com.sijan.barberReservation.DTO.appointment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;


import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {

    private Long barberId;
    private Long serviceId;
    private LocalDateTime appointmentDateTime;
    private String notes;
}

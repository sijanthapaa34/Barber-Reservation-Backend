package com.sijan.barberReservation.DTO.appointment;

import com.sijan.barberReservation.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer durationMinutes;
    private boolean available;

    // Identity (frontend-friendly)
    private String slotKey;

    // UI helpers
    private String displayTime;

}
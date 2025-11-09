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
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;
    private LocalDateTime dateTime;
    private boolean available;
    private AppointmentStatus status;

    // Additional context
    private Duration duration;
    private String displayTime;  // "10:30 AM - 11:15 AM"
    private String displayDate;  // "December 25, 2024"

    // Optional - for UI purposes
    private boolean isRecommended;
    private String unavailableReason;  // If not available
}
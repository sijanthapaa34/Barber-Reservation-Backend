package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarberScheduleDTO {

    private String barberName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isWorkingDay;
    private List<BreakTimeDTO> breaks;

    // Generated slots for the day
    private List<TimeSlotDTO> allSlots;
    private List<TimeSlotDTO> availableSlots;
    private List<TimeSlotDTO> bookedSlots;
}

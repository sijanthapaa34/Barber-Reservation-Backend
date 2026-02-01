package com.sijan.barberReservation.DTO.appointment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotsResponseDTO {
    private LocalDate date;
    private String barberName;
    private String barberShop;
    private List<String> serviceName;
    private Duration serviceDuration;
    private List<TimeSlotDTO> availableSlots;
    private List<TimeSlotDTO> bookedSlots;

    // Summary info
    private int totalAvailableSlots;
    private String nextAvailableSlot;
    private boolean hasAvailableSlots;
}
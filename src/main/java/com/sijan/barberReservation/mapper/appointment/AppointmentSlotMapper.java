package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.AvailableSlotsResponseDTO;
import com.sijan.barberReservation.DTO.appointment.TimeSlotDTO;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppointmentSlotMapper {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public TimeSlotDTO toTimeSlotDTO(Appointment appointment) {
            LocalDateTime dateTime = appointment.getScheduledTime();
            List<ServiceOffering> services = appointment.getServices();

            int totalMinutes = services.stream()
                    .mapToInt(ServiceOffering::getDurationMinutes)
                    .sum();

            return TimeSlotDTO.builder()
                    .dateTime(dateTime)
                    .date(dateTime.toLocalDate())
                    .startTime(dateTime.toLocalTime())
                    .endTime(dateTime.toLocalTime().plusMinutes(totalMinutes))
                    .available(false)
                    .status(appointment.getStatus())
                    .duration(Duration.ofMinutes(totalMinutes))
                    .displayTime(dateTime.toLocalTime().format(TIME_FORMATTER) + " - " +
                            dateTime.toLocalTime().plusMinutes(totalMinutes).format(TIME_FORMATTER))
                    .displayDate(dateTime.toLocalDate().format(DATE_FORMATTER))
                    .build();
    }

    public List<TimeSlotDTO> toTimeSlotDTOList(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toTimeSlotDTO)
                .collect(Collectors.toList());
    }

    public AvailableSlotsResponseDTO toAvailableSlotsResponse(
            Barber barber,
            List<ServiceOffering> services,
            LocalDate date,
            List<TimeSlotDTO> availableSlots,
            List<TimeSlotDTO> bookedSlots
    ) {
        AvailableSlotsResponseDTO response = new AvailableSlotsResponseDTO();
        response.setBarberName(barber.getName());
        response.setServiceName(services.stream()
                .map(ServiceOffering::getName)
                .collect(Collectors.toList()));

        int totalMinutes = services.stream()
                .mapToInt(ServiceOffering::getDurationMinutes)
                .sum();
        response.setServiceDuration(Duration.ofMinutes(totalMinutes));

        response.setDate(date);
        response.setAvailableSlots(availableSlots);
        response.setBookedSlots(bookedSlots);
        response.setTotalAvailableSlots(availableSlots.size());
        response.setNextAvailableSlot(availableSlots.isEmpty() ? null : availableSlots.get(0).getDisplayTime());
        response.setHasAvailableSlots(!availableSlots.isEmpty());

        return response;
    }


    public List<TimeSlotDTO> toAvailableSlots(List<LocalDateTime> availableSlotTimes, int totalDurationMinutes) {
        return availableSlotTimes.stream()
                .map(time -> {
                    LocalTime start = time.toLocalTime();
                    LocalTime end = start.plusMinutes(totalDurationMinutes);

                    return TimeSlotDTO.builder()
                            .dateTime(time)
                            .date(time.toLocalDate())
                            .startTime(start)
                            .endTime(end)
                            .available(true)
                            .duration(Duration.ofMinutes(totalDurationMinutes))
                            .displayTime(start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER))
                            .displayDate(time.toLocalDate().format(DATE_FORMATTER))
                            .build();
                })
                .toList();
    }
}

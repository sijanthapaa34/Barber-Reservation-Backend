package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.DetailsDTO;
import com.sijan.barberReservation.DTO.appointment.ServiceItemDTO;
import com.sijan.barberReservation.model.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentDetailsMapper {
    public DetailsDTO toDetailsDTO(Appointment appointment){

        List<ServiceItemDTO> serviceItems =
                appointment.getServices()
                        .stream()
                        .map(service ->
                                new ServiceItemDTO(
                                        service.getName(),
                                        service.getPrice()
                                )
                        )
                        .toList();
        DetailsDTO dto= new DetailsDTO();
        dto.setCustomerName(appointment.getCustomer().getName());
        dto.setBarberName(appointment.getBarber().getName());
        dto.setServices(serviceItems);
        dto.setScheduledTime(appointment.getScheduledTime());
        dto.setCheckInTime(appointment.getCheckInTime());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setStatus(appointment.getStatus().name());

        return dto;
    }
    public List<DetailsDTO> toDetailsDTO(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toDetailsDTO)
                .toList();
    }
}

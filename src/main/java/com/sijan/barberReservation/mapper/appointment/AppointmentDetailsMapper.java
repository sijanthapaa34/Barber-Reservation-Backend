package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.ServiceItemDTO;
import com.sijan.barberReservation.model.Appointment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentDetailsMapper {
    public AppointmentDetailsResponse toDTO(Appointment appointment){

        List<ServiceItemDTO> serviceItems =
                appointment.getServices()
                        .stream()
                        .map(service -> new ServiceItemDTO(
                                service.getId(),
                                service.getName(),
                                service.getPrice(),
                                service.getDurationMinutes()
                        ))
                        .toList();

        AppointmentDetailsResponse response = new AppointmentDetailsResponse();

        // Identity
        response.setAppointmentId(appointment.getId());

        response.setCustomerId(appointment.getCustomer().getId());
        response.setCustomerName(appointment.getCustomer().getName());

        response.setBarberId(appointment.getBarber().getId());
        response.setBarberName(appointment.getBarber().getName());

        response.setBarberShopId(appointment.getBarbershop().getId());
        response.setBarberShopName(appointment.getBarbershop().getName());

        // Services & pricing
        response.setServices(serviceItems);
        response.setTotalPrice(appointment.getTotalPrice());
        response.setTotalDurationMinutes(appointment.getTotalDurationMinutes());

        // Status & timing
        response.setStatus(appointment.getStatus());
        response.setScheduledTime(appointment.getScheduledTime());
        response.setCheckInTime(appointment.getCheckInTime());
        response.setCompletedTime(appointment.getCompletedTime());

        // Payment
        response.setPaymentStatus(appointment.getPaymentStatus());
        response.setPaidAmount(appointment.getPaidAmount());
        response.setPaymentMethod(appointment.getPaymentMethod());

        // Notes
        response.setCustomerNotes(appointment.getCustomerNotes());
        response.setBarberNotes(appointment.getBarberNotes());

        // Audit
        response.setCreatedAt(appointment.getCreatedAt());

        return response;
    }
    public List<AppointmentDetailsResponse> toDTOs(List<Appointment> appointments) {
        return appointments.stream()
                .map(this::toDTO)
                .toList();
    }
}

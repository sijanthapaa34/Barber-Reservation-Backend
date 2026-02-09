package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final ServiceMapper serviceMapper;

    public PageMapper(AppointmentDetailsMapper appointmentDetailsMapper, ServiceMapper serviceMapper) {
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.serviceMapper = serviceMapper;
    }

    public PageResponse<AppointmentDetailsResponse> toAppointmentPageResponse(Page<Appointment> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(appointmentDetailsMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    public PageResponse<ServiceDTO> toServicePageResponse(Page<ServiceOffering> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(serviceMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

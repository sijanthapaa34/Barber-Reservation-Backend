package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.DetailsDTO;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    private final AppointmentDetailsMapper appointmentDetailsMapper;

    public PageMapper(AppointmentDetailsMapper appointmentDetailsMapper) {
        this.appointmentDetailsMapper = appointmentDetailsMapper;
    }

    public PageResponse<DetailsDTO> toPageResponse(Page<Appointment> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(appointmentDetailsMapper::toDetailsDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

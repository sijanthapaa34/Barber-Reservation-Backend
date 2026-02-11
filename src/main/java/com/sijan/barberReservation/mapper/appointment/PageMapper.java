package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberLeaveMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final ServiceMapper serviceMapper;
    private final BarberLeaveMapper barberLeaveMapper;
    private final BarberMapper barberMapper;


    public PageMapper(AppointmentDetailsMapper appointmentDetailsMapper, ServiceMapper serviceMapper, BarberLeaveMapper barberLeaveMapper, BarberMapper barberMapper) {
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.serviceMapper = serviceMapper;
        this.barberLeaveMapper = barberLeaveMapper;
        this.barberMapper = barberMapper;
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
    public PageResponse<BarberDTO> toBarberPageResponse(Page<Barber> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(barberMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public PageResponse<BarberLeaveDTO> toBarberLeavePageResponse(Page<BarberLeave> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(barberLeaveMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    public PageResponse<BarberLeaveDTO> toCustomerPageResponse(Page<BarberLeave> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(barberLeaveMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

}

package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberLeaveMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final ServiceMapper serviceMapper;
    private final BarberLeaveMapper barberLeaveMapper;
    private final ApplicationMapper applicationMapper;
    private final BarberMapper barberMapper;
    private final CustomerMapper customerMapper;
    private final BarbershopMapper barbershopMapper;


    public PageMapper(AppointmentDetailsMapper appointmentDetailsMapper, ServiceMapper serviceMapper, BarberLeaveMapper barberLeaveMapper, ApplicationMapper applicationMapper, BarberMapper barberMapper, CustomerMapper customerMapper, BarbershopMapper barbershopMapper) {
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.serviceMapper = serviceMapper;
        this.barberLeaveMapper = barberLeaveMapper;
        this.applicationMapper = applicationMapper;
        this.barberMapper = barberMapper;
        this.customerMapper = customerMapper;
        this.barbershopMapper = barbershopMapper;
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
    public PageResponse<BarbershopDTO> toBarbershopPageResponse(Page<Barbershop> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(barbershopMapper::toDTO)
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
    public PageResponse<CustomerDTO> toCustomerPageResponse(Page<Customer> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(customerMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public PageResponse<ApplicationDetailResponse> toApplicationPageResponse(Page<Application> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(applicationMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

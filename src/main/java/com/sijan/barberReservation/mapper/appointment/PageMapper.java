package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.DTO.user.BarbershopDTO;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.mapper.Leave.LeaveMapper;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.mapper.review.ReviewMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PageMapper {

    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final ServiceMapper serviceMapper;
    private final ApplicationMapper applicationMapper;
    private final BarberMapper barberMapper;
    private final CustomerMapper customerMapper;
    private final BarbershopMapper barbershopMapper;
    private final ReviewMapper reviewMapper;
    private final LeaveMapper leaveMapper;

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

    public PageResponse<ReviewDTO> toReviewPageResponse(Page<Review> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(reviewMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
    public PageResponse<BarberLeaveDTO> toLeavePageResponse(Page<BarberLeave> page) {
        return new PageResponse<>(
                page.getContent()
                        .stream()
                        .map(leaveMapper::toDTO)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}

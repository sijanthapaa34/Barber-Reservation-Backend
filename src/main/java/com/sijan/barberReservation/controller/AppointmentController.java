package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.CreateAppointmentMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final BarberService barberService;
    private final CustomerService customerService;
    private final ServiceOfferingService serviceOfferingService;
    private final CreateAppointmentMapper createAppointmentMapper;
    private final PageMapper pageMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 AppointmentDetailsMapper appointmentDetailsMapper, BarberService barberService, CustomerService customerService, ServiceOfferingService serviceOfferingService,
                                 CreateAppointmentMapper createAppointmentMapper,
                                 PageMapper pageMapper) {
        this.appointmentService = appointmentService;
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.barberService = barberService;
        this.customerService = customerService;
        this.serviceOfferingService = serviceOfferingService;
        this.createAppointmentMapper = createAppointmentMapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDetailsResponse> findById(@PathVariable Long appointmentId){
        Appointment appointment = appointmentService.findById(appointmentId);
        return ResponseEntity.ok(appointmentDetailsMapper.toDTO(appointment));
    }
    @PutMapping("/{appointmentId}/reschedule")
    public ResponseEntity<AppointmentDetailsResponse> reschedule(@PathVariable Long appointmentId,
                                                                 @Valid @RequestBody RescheduleAppointmentRequest request){
        Appointment appointment = appointmentService.findById(appointmentId);
        AppointmentDetailsResponse response = appointmentDetailsMapper.toDTO(appointmentService.reschedule(appointment, request.getNewDateTime()));

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentDetailsResponse> book(@RequestBody CreateAppointmentRequest request,
                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Customer customer = customerService.findById(userPrincipal.getId());
        Appointment appointment = createAppointmentMapper.toAppointment(request);
        AppointmentDetailsResponse booked = appointmentDetailsMapper.toDTO(appointmentService.book(appointment, customer));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(booked);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> upcoming(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Customer customer = customerService.findById(userPrincipal.getId());
        Page<Appointment> result = appointmentService.getUpcoming(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("/past")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> past(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Customer customer = customerService.findById(userPrincipal.getId());
        Page<Appointment> result = appointmentService.getPast(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long appointmentId) {
        appointmentService.cancel(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{barberId}/availability")
    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
            @PathVariable Long barberId,
            @RequestParam @NotEmpty List<Long> serviceIds,
            @RequestParam @NotNull LocalDate date
    ) {

        Barber barber = barberService.findById(barberId);
        List<ServiceOffering> services = serviceOfferingService.findByIds(serviceIds);

        AvailableSlotsResponseDTO response =
                appointmentService.getAvailability(barber, services, date);

        return ResponseEntity.ok(response);
    }
}


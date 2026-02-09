package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.mapper.appointment.AppointmentSlotMapper;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.CreateMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final BarberService barberService;
    private final CustomerService customerService;
    private final ServiceOfferingService serviceOfferingService;
    private final CreateMapper createAppointmentMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final PageMapper pageMapper;

    public AppointmentController(AppointmentService appointmentService,
                                 AppointmentDetailsMapper appointmentDetailsMapper, BarberService barberService, CustomerService customerService, ServiceOfferingService serviceOfferingService,
                                 CreateMapper createAppointmentMapper, AppointmentSlotMapper appointmentSlotMapper,
                                 PageMapper pageMapper) {
        this.appointmentService = appointmentService;
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.barberService = barberService;
        this.customerService = customerService;
        this.serviceOfferingService = serviceOfferingService;
        this.createAppointmentMapper = createAppointmentMapper;
        this.appointmentSlotMapper = appointmentSlotMapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDetailsResponse> findById(@PathVariable Long appointmentId){
        Appointment appointment = appointmentService.findById(appointmentId);
        return ResponseEntity.ok(appointmentDetailsMapper.toDTO(appointment));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentDetailsResponse> book(@RequestBody CreateAppointmentRequest request,
            Authentication authentication) {
        Long customerId = Long.valueOf(authentication.getName());
        Customer customer = customerService.findById(customerId);
        Appointment appointment = createAppointmentMapper.toAppointment(request);
        AppointmentDetailsResponse booked = appointmentDetailsMapper.toDTO(appointmentService.book(appointment, customer));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(booked);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> upcoming(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             Authentication authentication) {
        Long customerId = Long.valueOf(authentication.getName());
        Customer customer = customerService.findById(customerId);
        Page<Appointment> result = appointmentService.getUpcoming(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("/past")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> past(@RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size,
                                                                         Authentication authentication) {

        Long customerId = Long.valueOf(authentication.getName());
        Customer customer = customerService.findById(customerId);
        Page<Appointment> result = appointmentService.getPast(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/availability")
    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
            @RequestParam Long barberId,
            @RequestParam List<Long> serviceIds,
            @RequestParam LocalDate date
    ) {
        Barber barber = barberService.findById(barberId);
        List<ServiceOffering> services = serviceIds.stream()
                .map(serviceOfferingService::findById)
                .toList();

        List<LocalDateTime> availableSlotTimes =
                appointmentService.computeAvailableSlots(barber, date, services);

        List<Appointment> bookedAppointments =
                appointmentService.getBookedAppointments(barber, date);

        List<TimeSlotDTO> availableSlots = appointmentSlotMapper.toAvailableSlots(
                availableSlotTimes,
                services.stream().mapToInt(ServiceOffering::getDurationMinutes).sum()
        );

        List<TimeSlotDTO> bookedSlots = appointmentSlotMapper.toTimeSlotDTOList(bookedAppointments);

        AvailableSlotsResponseDTO response = appointmentSlotMapper.toAvailableSlotsResponse(
                barber,
                services,
                date,
                availableSlots,
                bookedSlots
        );

        return ResponseEntity.ok(response);
    }

}


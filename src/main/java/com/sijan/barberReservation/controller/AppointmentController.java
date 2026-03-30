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
import com.sijan.barberReservation.service.UserService; // Added
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // Added
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final BarberService barberService;
    private final CustomerService customerService;
    private final ServiceOfferingService serviceOfferingService;
    private final CreateAppointmentMapper createAppointmentMapper;
    private final PageMapper pageMapper;
    private final UserService userService; // Added UserService

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

//    @PostMapping
//    @PreAuthorize("hasRole('CUSTOMER')")
//    public ResponseEntity<AppointmentDetailsResponse> book(@RequestBody CreateAppointmentRequest request,
//                                                           @AuthenticationPrincipal UserPrincipal userPrincipal) {
//        Customer customer = customerService.findById(userPrincipal.getId());
//        Appointment appointment = createAppointmentMapper.toAppointment(request);
//        AppointmentDetailsResponse booked = appointmentDetailsMapper.toDTO(appointmentService.book(appointment, customer));
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(booked);
//    }

    @GetMapping("/upcoming")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> upcomingByCustomer(@RequestParam(defaultValue = "0") int page,
                                                                                       @RequestParam(defaultValue = "10") int size,
                                                                                       @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Customer customer = customerService.findById(userPrincipal.getId());
        Page<Appointment> result = appointmentService.getUpcomingByCustomer(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("/past")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> pastByCustomer(@RequestParam(defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "10") int size,
                                                                                   @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Customer customer = customerService.findById(userPrincipal.getId());
        Page<Appointment> result = appointmentService.getPastByCustomer(customer, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("barber/{barberId}/upcoming")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> upcomingByBarber(@PathVariable Long barberId,@RequestParam(defaultValue = "0") int page,
                                                                                     @RequestParam(defaultValue = "10") int size
    ) {
        Barber barber = barberService.findById(barberId);
        Page<Appointment> result = appointmentService.getUpcomingByBarber(barber, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("barber/{barberId}/past")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> pastByBarber(@PathVariable Long barberId,@RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size
    ) {
        Barber barber = barberService.findById(barberId);
        Page<Appointment> result = appointmentService.getPastByBarber(barber, page, size);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    // UPDATED CANCEL METHOD
    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Fetch user to get their name for the email notification
        User user = userService.findById(userPrincipal.getId());

        // Call service with ID and Name
        appointmentService.cancel(appointmentId, user.getName());

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

    @GetMapping("/barber/{barberId}")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> getByBarber(
            @PathVariable Long barberId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Appointment> response = appointmentService.getBarberAppointments(barberService.findById(barberId), startDate, endDate, pageable);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(response));
    }

    @GetMapping("/barber/{barberId}/earnings")
    public ResponseEntity<Double> getEarnings(
            @PathVariable Long barberId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        Double earnings = appointmentService.getEarnings(barberService.findById(barberId), startDate, endDate);
        return ResponseEntity.ok(earnings);
    }
}
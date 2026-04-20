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

    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findById(userPrincipal.getId());
        appointmentService.cancel(appointmentId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{appointmentId}/refund-preview")
    public ResponseEntity<java.util.Map<String, Object>> getRefundPreview(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(appointmentService.getRefundPreview(appointmentId, userPrincipal.getId()));
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

    // Shop Admin Endpoints
    @GetMapping("/shop/{shopId}/all")
    @PreAuthorize("hasRole('SHOP_ADMIN') or hasRole('MAIN_ADMIN')")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> getShopAppointments(
            @PathVariable Long shopId,
            @RequestParam(required = false) String filter, // "today", "upcoming", "past", or null for all
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> result = appointmentService.getShopAppointments(shopId, filter, pageable);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    // Notify Customer (Manual reminder for barbers)
    @PostMapping("/{appointmentId}/notify")
    @PreAuthorize("hasRole('BARBER') or hasRole('SHOP_ADMIN')")
    public ResponseEntity<java.util.Map<String, String>> notifyCustomer(
            @PathVariable Long appointmentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Appointment appointment = appointmentService.findById(appointmentId);
        appointmentService.sendManualReminder(appointment);
        
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Notification sent to customer successfully",
            "customerName", appointment.getCustomer().getName()
        ));
    }
}
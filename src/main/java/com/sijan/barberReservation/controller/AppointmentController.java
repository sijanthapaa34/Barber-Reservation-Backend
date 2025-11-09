package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController{

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Book a new appointment
    @PostMapping("/book")
    public ResponseEntity<DetailsDTO> bookNewAppointment(
            @RequestBody CreateAppointmentRequest request) {

        String email = getCurrentUserEmail();
        DetailsDTO booked = appointmentService.bookNewAppointment(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(booked);
    }

    // View appointment by ID
    @GetMapping("/{id}")
    public ResponseEntity<DetailsDTO> viewAppointmentDetails(
            @PathVariable Long id) {
        String email = getCurrentUserEmail();
        DetailsDTO details = appointmentService.viewAppointmentDetails(id, email);
        return ResponseEntity.ok(details);
    }

    // Get user's appointments
    @GetMapping("/my-appointments")
    public ResponseEntity<List<DetailsDTO>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(appointmentService.getUserAppointments(email, page, size));
    }

    // Cancel an appointment
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointment(
            @PathVariable Long id,
            @RequestBody CancelAppointmentRequest request) {
        String email = getCurrentUserEmail();
        String result = appointmentService.cancelAppointment(id, email, request.getReason());
        return ResponseEntity.ok(result);
    }

    // Reschedule an appointment
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<DetailsDTO> rescheduleAppointment(
            @PathVariable Long id,
            @RequestBody RescheduleAppointmentRequest request) {
        String email = getCurrentUserEmail();
        DetailsDTO rescheduled = appointmentService.rescheduleAppointment(id, email, request);
        return ResponseEntity.ok( rescheduled);
    }
    @GetMapping("/admin")
    public ResponseEntity<List<DetailsDTO>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = getCurrentUserEmail();
        return ResponseEntity.ok(appointmentService.getAllAppointments(page, size));
    }

    @GetMapping("/available-slots")
    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
            @RequestParam Long barberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {
        String email = getCurrentUserEmail();
        AvailableSlotsResponseDTO slots = appointmentService.getAvailableSlots(barberId, date, serviceId);
        return ResponseEntity.ok(slots);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getUsername();
        }
        throw new RuntimeException("User not authenticated");
    }

}


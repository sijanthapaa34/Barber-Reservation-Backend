package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.appointment.AvailableSlotsResponseDTO;
import com.sijan.barberReservation.DTO.appointment.DetailsDTO;
import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barberService;
    private final AppointmentService appointmentService;

    public BarberController (BarberService barberService, AppointmentService appointmentService) {
        this.barberService = barberService;
        this.appointmentService = appointmentService;
    }

    // GET /api/barbers/me - Get current barber profile
    @GetMapping("/me")
    public ResponseEntity<BarberDTO> getMyProfile() {
        String email = getCurrentUserEmail();
        BarberDTO dto = barberService.getBarberProfile(email);
        return ResponseEntity.ok(dto);
    }

    // PUT /api/barbers/me - Update barber profile (name, phone, bio)
    @PutMapping("/me")
    public ResponseEntity<BarberDTO> updateMyProfile(
            @RequestBody UpdateUserRequest request) {
        String email = getCurrentUserEmail();
        BarberDTO updated = barberService.updateBarberProfile(email, request);
        return ResponseEntity.ok(updated);
    }

    // GET /api/barbers/me/appointments - Get today's appointments
    @GetMapping("/me/appointments")
    public ResponseEntity<List<DetailsDTO>> getTodayAppointments(
            @RequestParam(required = false) LocalDate date) {
        String email = getCurrentUserEmail();
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<DetailsDTO> appointments = appointmentService.getBarberAppointments(email, targetDate);
        return ResponseEntity.ok(appointments);
    }

    // POST /api/barbers/me/leave - Apply for leave
    @PostMapping("/me/leave")
    public ResponseEntity<String> applyForLeave(
            @RequestBody LeaveRequestDTO request) {
        String email = getCurrentUserEmail();
        // Validate dates
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ResponseEntity.badRequest().body("Start date cannot be after end date");
        }

        // Delegate to service
        barberService.applyForLeave(email, request.getStartDate(), request.getEndDate(), request.getReason());
        return ResponseEntity.ok("Leave request submitted successfully");
    }


    // POST /api/barbers/me/change-password - Change barber password
    @PutMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request) {
        String email = getCurrentUserEmail();
        barberService.changePassword(email, request);
        return ResponseEntity.ok().build();
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getUsername();
        }
        throw new RuntimeException("User not authenticated");
    }
}
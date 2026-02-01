package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.appointment.DetailsDTO;
import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.DTO.user.RegisterBarberRequest;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.UpdateUserRequestMapper;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.AppointmentService;
import jakarta.validation.Valid;
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
    private final BarberMapper barberMapper;
    private final AppointmentDetailsMapper appointmentMapper;
    private final UpdateUserRequestMapper requestMapper;

    public BarberController (BarberService barberService, AppointmentService appointmentService, BarberMapper barberMapper, AppointmentDetailsMapper appointmentMapper, UpdateUserRequestMapper requestMapper) {
        this.barberService = barberService;
        this.appointmentService = appointmentService;
        this.barberMapper = barberMapper;
        this.appointmentMapper = appointmentMapper;
        this.requestMapper = requestMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDTO> getMyProfile(@PathVariable Long id) {
        Barber barber = barberService.findById(id);
        return ResponseEntity.ok(barberMapper.toDTO(barber));
    }

    @PutMapping("/me")
    public ResponseEntity<BarberDTO> updateMyProfile(
            @RequestBody UpdateUserRequest req) {
        Barber barber = requestMapper.toEntity(req);
        BarberDTO updated = barberMapper.toDTO(barberService.updateBarberProfile(barber));
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{barbershopId}/barbers")
    public ResponseEntity<BarberDTO> register(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barbershopId,
            @RequestBody RegisterBarberRequest request) {
        Barber barber = barberMapper.toEntity(request);
        BarberDTO newBarber = barberMapper.toDTO(barberService.register(adminId, barbershopId, barber));
        return ResponseEntity.ok(newBarber);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<DetailsDTO>> getTodayAppointments(@PathVariable Long id,
            @RequestParam(required = false) LocalDate date) {
        Barber barber = barberService.findById(id);
        List<Appointment> appointments = appointmentService.getBarberAppointments(barber, date);
        return ResponseEntity.ok(appointmentMapper.toDetailsDTO(appointments));
    }

    // Get all barbers for a barbershop
    @GetMapping("/{barbershopId}/barbers")
    public ResponseEntity<List<BarberDTO>> getAllBarbers(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barbershopId) {
        List<Barber> barbers = barberService.getAll(adminId, barbershopId);
        return ResponseEntity.ok(barberMapper.toDTOs(barbers));
    }

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
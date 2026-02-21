package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.UpdateUserRequestMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.AdminService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarbershopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final AppointmentService appointmentService;
    private final AdminService adminService;
    private final BarberMapper barberMapper;
    private final PageMapper pageMapper;
    private final AppointmentDetailsMapper appointmentMapper;
    private final UpdateUserRequestMapper requestMapper;

    public BarberController (BarberService barberService, BarbershopService barbershopService, AppointmentService appointmentService, AdminService adminService, BarberMapper barberMapper, PageMapper pageMapper, AppointmentDetailsMapper appointmentMapper, UpdateUserRequestMapper requestMapper) {
        this.barberService = barberService;
        this.barbershopService = barbershopService;
        this.appointmentService = appointmentService;
        this.adminService = adminService;
        this.barberMapper = barberMapper;
        this.pageMapper = pageMapper;
        this.appointmentMapper = appointmentMapper;
        this.requestMapper = requestMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDTO> getMyProfile(@PathVariable Long id) {
        Barber barber = barberService.findById(id);
        return ResponseEntity.ok(barberMapper.toDTO(barber));
    }

    @GetMapping("/barbershop/{barbershopId}")
    public ResponseEntity<PageResponse<BarberDTO>> getByBarbershop(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @PathVariable Long barbershopId
    ) {
        Barbershop barbershop = barbershopService.findById(barbershopId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Barber> barbers = barberService.findByBarberShop(barbershop, pageable);
        return ResponseEntity.ok(pageMapper.toBarberPageResponse(barbers));
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentDetailsResponse>> getTodayAppointments(@PathVariable Long id,
                                                                                 @RequestParam(required = false) LocalDate date) {
        Barber barber = barberService.findById(id);
        List<Appointment> appointments = appointmentService.getBarberAppointments(barber, date);
        return ResponseEntity.ok(appointmentMapper.toDTOs(appointments));
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
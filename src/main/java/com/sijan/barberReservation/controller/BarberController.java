package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.BarberDTO;
import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.DTO.user.UpdateBarberRequest;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarbershopService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {

    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final AppointmentService appointmentService;
    private final BarberMapper barberMapper;
    private final PageMapper pageMapper;
    private final AppointmentDetailsMapper appointmentMapper;

    public BarberController (BarberService barberService, BarbershopService barbershopService, AppointmentService appointmentService, BarberMapper barberMapper, PageMapper pageMapper, AppointmentDetailsMapper appointmentMapper) {
        this.barberService = barberService;
        this.barbershopService = barbershopService;
        this.appointmentService = appointmentService;
        this.barberMapper = barberMapper;
        this.pageMapper = pageMapper;
        this.appointmentMapper = appointmentMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDTO> findById(@PathVariable Long id) {
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

        barberService.applyForLeave(email, request.getStartDate(), request.getEndDate(), request.getReason());
        return ResponseEntity.ok("Leave request submitted successfully");
    }

    @PutMapping("/{barberId}/update")
    public ResponseEntity<BarberDTO> updateProfile(@PathVariable Long barberId, @RequestBody UpdateBarberRequest request) {
        Barber barber = barberService.findById(barberId);
        Barber updated = barberService.update(barber, request.getName(), request.getPhone(), request.getBio(), request.getSkills(), request.getExperienceYears());
        return ResponseEntity.ok(barberMapper.toDTO(updated));
    }


    @PutMapping("/{barberId}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long barberId, @RequestBody ChangePasswordRequest request) {
        barberService.changePassword(barberService.findById(barberId), request.getCurrentPassword(), request.getNewPassword());
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
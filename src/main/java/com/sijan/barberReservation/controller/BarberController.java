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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final BarberMapper barberMapper;
    private final PageMapper pageMapper;

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

    @PatchMapping("/{barberId}/update")
    public ResponseEntity<BarberDTO> updateProfile(@PathVariable Long barberId, @RequestBody UpdateBarberRequest request) {
        Barber barber = barberService.findById(barberId);
        Barber updated = barberService.update(barber,request.getBio(), request.getSkills(), request.getExperienceYears(), request.getWorkImages(), request.getCommissionRate());
        return ResponseEntity.ok(barberMapper.toDTO(updated));
    }

    @PatchMapping("/{barberId}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long barberId, @RequestBody ChangePasswordRequest request) {
        barberService.changePassword(barberService.findById(barberId), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{barbershopId}/activate/{barberId}")
    public ResponseEntity<Void> activate(
            @PathVariable Long barbershopId,
            @PathVariable Long barberId) {
        Barber barber = barberService.findById(barberId);
        if (!barber.getBarbershop().getId().equals(barbershopId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This barber does not belong to the specified shop.");
        }
        barberService.activateBarber(barber);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{barbershopId}/deactivate/{barberId}")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long barbershopId,
            @PathVariable Long barberId) {
        Barber barber = barberService.findById(barberId);
        if (!barber.getBarbershop().getId().equals(barbershopId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This barber does not belong to the specified shop.");
        }
        barberService.deactivateBarber(barber);
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
package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.service.BarberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private BarberService barberService;

    @PostMapping("/{barberId}")
    public ResponseEntity<String> applyForLeave(
            @PathVariable Long barberId,
            @RequestBody LeaveRequestDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start date and end date are required");
        }
        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ResponseEntity.badRequest().body("Start date cannot be after end date");
        }

        barberService.

        barberService.applyForLeave(barber, request.getStartDate(), request.getEndDate(), request.getReason());
        return ResponseEntity.ok("Leave request submitted successfully");
    }
}

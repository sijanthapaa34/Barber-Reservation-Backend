package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.BarberLeaveDTO;
import com.sijan.barberReservation.DTO.user.LeaveRequestDTO;
import com.sijan.barberReservation.mapper.Leave.LeaveMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.service.BarberLeaveService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarbershopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final LeaveMapper leaveMapper;
    private final PageMapper pageMapper;
    private final BarberLeaveService barberLeaveService;

    @PostMapping("/{barberId}")
    public ResponseEntity<String> applyForLeave(
            @PathVariable Long barberId,
            @RequestBody LeaveRequestDTO request) {
        Barber barber = barberService.findById(barberId);
        BarberLeave leave = leaveMapper.toEntity(request);
        barberLeaveService.applyForLeave(barber, leave);
        return ResponseEntity.ok("Leave request submitted successfully");
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<PageResponse<BarberLeaveDTO>> getShopLeaves(@PathVariable Long shopId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Barbershop shop = barbershopService.findById(shopId);
        Page<BarberLeave> leavePage = barberLeaveService.getLeavesByShop(shop,pageable);
        return ResponseEntity.ok(pageMapper.toLeavePageResponse(leavePage));
    }

    // Get leaves for a specific Barber
    @GetMapping("/barber/{barberId}")
    public ResponseEntity<PageResponse<BarberLeaveDTO>> getBarberLeaves(@PathVariable Long barberId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Barber barber = barberService.findById(barberId);
        Page<BarberLeave> leavePage = barberLeaveService.getLeavesByBarber(barber,pageable);
        return ResponseEntity.ok(pageMapper.toLeavePageResponse(leavePage));
    }

    // Shop Admin approves leave
    @PutMapping("/{leaveId}/approve")
    public ResponseEntity<BarberLeaveDTO> approveLeave(@PathVariable Long leaveId) {
        BarberLeave leave = barberLeaveService.findById(leaveId);
        BarberLeave approvedLeave = barberLeaveService.approveLeave(leave);
        return ResponseEntity.ok(leaveMapper.toDTO(approvedLeave));
    }

    // Shop Admin rejects leave
    @PutMapping("/{leaveId}/reject")
    public ResponseEntity<BarberLeaveDTO> rejectLeave(@PathVariable Long leaveId) {
        BarberLeave leave = barberLeaveService.findById(leaveId);
        BarberLeave rejectedLeave = barberLeaveService.rejectLeave(leave);
        return ResponseEntity.ok(leaveMapper.toDTO(rejectedLeave));
    }
}

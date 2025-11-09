package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.AvailableSlotsResponseDTO;
import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AppointmentService appointmentService;

    public CustomerController(CustomerService customerService, AppointmentService appointmentService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
    }

    // GET /api/customers/me - Get current customer profile
    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> getMyProfile() {
        String email = getCurrentUserEmail();
        CustomerDTO dto = customerService.getCustomerProfile(email);
        return ResponseEntity.ok(dto);
    }

    // PUT /api/customers/me - Update customer info
    @PutMapping("/me")
    public ResponseEntity<CustomerDTO> updateMyProfile(@RequestBody UpdateUserRequest request) {
        String email = getCurrentUserEmail();
        CustomerDTO updated = customerService.updateCustomerProfile(email, request);
        return ResponseEntity.ok(updated);
    }

    // GET /api/customers/me/availability - Get available slots for a date
    @GetMapping("/me/availability")
    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
            @RequestParam Long barberId,
            @RequestParam LocalDate date,
            @RequestParam Long serviceId) {

        AvailableSlotsResponseDTO slots = appointmentService.getAvailableSlots(barberId, date, serviceId);
        return ResponseEntity.ok(slots);
    }

    // PUT /api/customers/me/change-password - Change password
    @PutMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = getCurrentUserEmail();
        customerService.changePassword(email, request);
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
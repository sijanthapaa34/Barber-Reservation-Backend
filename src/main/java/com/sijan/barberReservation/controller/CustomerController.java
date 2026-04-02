package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    // GET /api/customers/me - Get current customer profile
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> findById(@PathVariable Long customerId) {
        CustomerDTO dto = customerMapper.toDTO(customerService.findById(customerId));
        return ResponseEntity.ok(dto);
    }

    // PUT /api/customers/me - Update customer info
    @PutMapping("/{customerId}/update")
    public ResponseEntity<CustomerDTO> updateMyProfile(@PathVariable Long customerId, @RequestBody UpdateUserRequest request) {
        Customer customer = customerService.findById(customerId);
        Customer updated = customerService.update(customer, request.getName(), request.getPhone());
        return ResponseEntity.ok(customerMapper.toDTO(updated));
    }

    // PUT /api/customers/me/change-password - Change password
    @PutMapping("/{customerId}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long customerId, @RequestBody ChangePasswordRequest request) {
        Customer customer = customerService.findById(customerId);
        customerService.changePassword(customer, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    // GET /api/customers/{customerId}/loyalty - Get loyalty points summary
    @GetMapping("/{customerId}/loyalty")
    public ResponseEntity<java.util.Map<String, Object>> getLoyalty(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId);
        int points = customer.getPoints() != null ? customer.getPoints() : 0;
        int pointsToFree = Math.max(0, 100 - (points % 100));
        int freeAppointmentsEarned = points / 100;

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("points", points);
        result.put("pointsToNextReward", pointsToFree);
        result.put("freeAppointmentsEarned", freeAppointmentsEarned);
        result.put("progressPercent", (points % 100));
        result.put("rule", "Spend Rs. 100 = 1 point. Collect 100 points = 1 free appointment!");
        return ResponseEntity.ok(result);
    }
}
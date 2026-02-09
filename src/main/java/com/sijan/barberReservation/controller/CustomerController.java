package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarberShopService;
import com.sijan.barberReservation.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AppointmentService appointmentService;
    private final BarberService barberService;
    private final BarberShopService barberShopService;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerService customerService, AppointmentService appointmentService, BarberService barberService, BarberShopService barberShopService, CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
        this.barberService = barberService;
        this.barberShopService = barberShopService;
        this.customerMapper = customerMapper;
    }

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

        Customer updated = customerService.update(
                customer,
                request.getName(),
                request.getPhone()
        );

        return ResponseEntity.ok(customerMapper.toDTO(updated));
    }

    // PUT /api/customers/me/change-password - Change password
    @PutMapping("/{customerId}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long customerId, @RequestBody ChangePasswordRequest request) {
        Customer customer = customerService.findById(customerId);
        customerService.changePassword(
                customer,
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok().build();
    }
}
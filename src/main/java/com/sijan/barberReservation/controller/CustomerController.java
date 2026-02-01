package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.DTO.user.UpdateUserRequest;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
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
    @GetMapping("/me")
    public ResponseEntity<CustomerDTO> getMyProfile() {
        String email = getCurrentUserEmail();
        CustomerDTO dto = customerMapper.toDTO(customerService.getCustomerProfile(email));
        return ResponseEntity.ok(dto);
    }

    // PUT /api/customers/me - Update customer info
    @PutMapping("/me")
    public ResponseEntity<CustomerDTO> updateMyProfile(@RequestBody UpdateUserRequest request) {
        String email = getCurrentUserEmail();
        CustomerDTO updated = customerMapper.toDTO(customerService.updateCustomerProfile(email, request));
        return ResponseEntity.ok(updated);
    }

    // GET /api/customers/me/availability - Get available slots for a date
//    @GetMapping("/me/availability")
//    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
//            @RequestParam Long shopId,
//            @RequestParam Long barberId,
//            @RequestParam LocalDate date,
//            @RequestParam List<Long> serviceId) {
//
//        BarberShop shop = barberShopService.findById(shopId);
//        Barber barber = barberService.findById(barberId);
//        AvailableSlotsResponseDTO slots = appointmentService.getAvailableSlotsEntity(barber, shop, date, serviceId);
//        return ResponseEntity.ok(slots);
//    }

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
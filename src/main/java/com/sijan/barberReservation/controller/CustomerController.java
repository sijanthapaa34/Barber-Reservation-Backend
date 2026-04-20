package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.CustomerDTO;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.CustomerMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;
    private final PageMapper pageMapper;

    // Get customer by ID (for profile)
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SHOP_ADMIN') or hasRole('MAIN_ADMIN')")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId);
        return ResponseEntity.ok(customerMapper.toDTO(customer));
    }

    // Get customer loyalty data
    @GetMapping("/{customerId}/loyalty")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SHOP_ADMIN') or hasRole('MAIN_ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> getCustomerLoyalty(@PathVariable Long customerId) {
        Customer customer = customerService.findById(customerId);
        
        int points = customer.getPoints() != null ? customer.getPoints() : 0;
        int freeAppointmentThreshold = 100;
        int freeAppointmentsEarned = points / freeAppointmentThreshold;
        int pointsToNextReward = freeAppointmentThreshold - (points % freeAppointmentThreshold);
        int progressPercent = (points % freeAppointmentThreshold);
        
        java.util.Map<String, Object> loyaltyData = new java.util.HashMap<>();
        loyaltyData.put("points", points);
        loyaltyData.put("freeAppointmentsEarned", freeAppointmentsEarned);
        loyaltyData.put("pointsToNextReward", pointsToNextReward);
        loyaltyData.put("progressPercent", progressPercent);
        
        return ResponseEntity.ok(loyaltyData);
    }

    // Get all customers (Main Admin)
    @GetMapping
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<PageResponse<CustomerDTO>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalBookings") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Customer> customers = customerService.findAll(pageable);
        return ResponseEntity.ok(pageMapper.toCustomerPageResponse(customers));
    }

    // Get regular customers (customers with 3+ bookings) - Main Admin
    @GetMapping("/regular")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<PageResponse<CustomerDTO>> getRegularCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "totalBookings"));
        Page<Customer> customers = customerService.findRegularCustomers(pageable);
        return ResponseEntity.ok(pageMapper.toCustomerPageResponse(customers));
    }

    // Get customers for a specific shop (Shop Admin)
    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasRole('SHOP_ADMIN')")
    public ResponseEntity<PageResponse<CustomerDTO>> getShopCustomers(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "totalBookings") String sortBy,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Page<Customer> customers = customerService.findByShop(shopId, pageable);
            PageResponse<CustomerDTO> response = pageMapper.toCustomerPageResponse(customers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching shop customers: " + e.getMessage(), e);
        }
    }

    // Get regular customers for a specific shop (Shop Admin)
    @GetMapping("/shop/{shopId}/regular")
    @PreAuthorize("hasRole('SHOP_ADMIN')")
    public ResponseEntity<PageResponse<CustomerDTO>> getShopRegularCustomers(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Use unsorted pageable for native queries
        Pageable pageable = PageRequest.of(page, size);
        Page<Customer> customers = customerService.findRegularCustomersByShop(shopId, pageable);
        return ResponseEntity.ok(pageMapper.toCustomerPageResponse(customers));
    }
}

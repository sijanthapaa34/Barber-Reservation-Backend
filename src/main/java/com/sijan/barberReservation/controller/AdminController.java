package com.sijan.barberReservation.controller;


import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.AdminService;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final AppointmentService appointmentService;
    private final BarberService barberService;
    private final CustomerService customerService;
    private final PageMapper pageMapper;


    public AdminController(AdminService adminService, AppointmentService appointmentService, BarberService barberService, CustomerService customerService, PageMapper pageMapper) {
        this.adminService = adminService;
        this.appointmentService = appointmentService;
        this.barberService = barberService;
        this.customerService = customerService;
        this.pageMapper = pageMapper;
    }
    private Admin getCurrentAdmin(Authentication authentication) {
        Long adminId =  Long.valueOf(authentication.getName());
        return adminService.findById(adminId);
    }

    @GetMapping("/barbers")
    public ResponseEntity<PageResponse<BarberDTO>> getBarbersByBarbershop(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Barber> barbers = barberService.findByBarberShop(admin, pageable);
        return ResponseEntity.ok(barbers);
    }

    @PutMapping("/barbers/{barberId}/activate")
    public ResponseEntity<String> activateBarber(
            @PathVariable Long barberId) {
        Barber barber = barberService.findById(barberId);
        barberService.activateBarber(barber);
        return ResponseEntity.ok("Barber activated successfully");
    }

    @PutMapping("/barbers/{barberId}/deactivate")
    public ResponseEntity<String> deactivateBarber(
            @PathVariable Long barberId) {
        Barber barber = barberService.findById(barberId);
        barberService.deactivateBarber(barber);
        return ResponseEntity.ok("Barber deactivated successfully");
    }
    @GetMapping("/appointment")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> getAllAppointment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> result = appointmentService.getAppointmentsForAdmin(admin, pageable);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }


    // GET /api/admin/customers/frequent - Get frequent customers
    @GetMapping("/customers/frequent")
    public ResponseEntity<PageResponse<FrequentCustomerDTO>> getFrequentCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Customer> customers = customerService.getFrequentCustomers(admin, pageable);
        return ResponseEntity.ok(customers);
    }

    // GET /api/admin/barbers/leaves - View all barber leave requests
    @GetMapping("/barbers/leaves")
    public ResponseEntity<PageResponse<BarberLeaveDTO>> getAllBarberLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        List<BarberLeaveDTO> leaves = barberService.getAllLeaves(admin, pageable);
        return ResponseEntity.ok(leaves);
    }

    // PUT /api/admin/barbers/{barberId}/leaves/{leaveId}/approve
    @PutMapping("/barbers/{barberId}/leaves/{leaveId}/approve")
    public ResponseEntity<String> approveLeave(
            @PathVariable Long barberId,
            @PathVariable Long leaveId,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        barberService.updateLeaveStatus(leaveId, barberId, LeaveStatus.APPROVED, admin);
        return ResponseEntity.ok("Leave approved successfully");
    }

    // PUT /api/admin/barbers/{barberId}/leaves/{leaveId}/reject

    @PutMapping("/barbers/{barberId}/leaves/{leaveId}/reject")
    public ResponseEntity<String> rejectLeave(
            @PathVariable Long barberId,
            @PathVariable Long leaveId,
            Authentication authentication
    ) {
        Admin admin = getCurrentAdmin(authentication);
        adminService.updateLeaveStatus(leaveId, barberId, LeaveStatus.REJECTED, admin);
        return ResponseEntity.ok("Leave rejected successfully");
    }
}
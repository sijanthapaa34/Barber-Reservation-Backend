package com.sijan.barberReservation.controller;


import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.service.ServiceRequest;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.model.LeaveStatus;
import com.sijan.barberReservation.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    // POST /api/admin/barbers - Add new barber
//    @PostMapping("/barbers")
//    public ResponseEntity<BarberDTO> addBarber(
//            @RequestHeader("X-User-ID") Long adminId,
//            @RequestBody @Valid CreateBarberRequest request) {
//
//        BarberDTO newBarber = adminService.addBarber(adminId, request);
//        return ResponseEntity.status(201).body(newBarber);
//    }

    // GET /api/admin/barbers - Get all barbers (with status)
    @GetMapping("/barbers")
    public ResponseEntity<List<BarberDTO>> getAllBarbers(
            @RequestHeader("X-User-ID") Long adminId
    ) {
        List<BarberDTO> barbers = adminService.getAllBarbers();
        return ResponseEntity.ok(barbers);
    }

    // PUT /api/admin/barbers/{id}/activate - Activate barber
    @PutMapping("/barbers/{barberId}/activate")
    public ResponseEntity<String> activateBarber(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberId) {
        adminService.activateBarber(barberId);
        return ResponseEntity.ok("Barber activated successfully");
    }

    // PUT /api/admin/barbers/{id}/deactivate - Deactivate barber
    @PutMapping("/barbers/{barberId}/deactivate")
    public ResponseEntity<String> deactivateBarber(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberId) {
        adminService.deactivateBarber(barberId);
        return ResponseEntity.ok("Barber deactivated successfully");
    }

    // GET /api/admin/services - Get all services
    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getAllServices(
            @RequestHeader("X-User-ID") Long adminId) {
        List<ServiceDTO> services = adminService.getAllServices(adminId);
        return ResponseEntity.ok(services);
    }

    // POST /api/admin/services - Add new service
    @PostMapping("/services")
    public ResponseEntity<ServiceDTO> addService(
            @RequestHeader("X-User-ID") Long adminId,
            @RequestBody ServiceRequest request) {
        ServiceDTO service = adminService.addService(adminId, request);
        return ResponseEntity.status(201).body(service);
    }

    // PUT /api/admin/services/{id} - Update service
    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceDTO> updateService(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long id,
            @RequestBody ServiceRequest request) {
        ServiceDTO updated = adminService.updateService(adminId, id, request);
        return ResponseEntity.ok(updated);
    }

    // PUT /api/admin/services/{id}/activate - Reactivate a service
    @PutMapping("/services/{id}/activate")
    public ResponseEntity<String> activateService(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long id) {
        adminService.activateService(adminId, id);
        return ResponseEntity.ok("Service activated successfully");
    }

    // PUT /api/admin/services/{id}/deactivate - Deactivate service
    @PutMapping("/services/{id}/deactivate")
    public ResponseEntity<String> deactivateService(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long id) {
        adminService.deactivateService(adminId, id);
        return ResponseEntity.ok("Service deactivated successfully");
    }

    // GET /api/admin/customers/frequent - Get frequent customers
    @GetMapping("/customers/frequent")
    public ResponseEntity<List<FrequentCustomerDTO>> getFrequentCustomers(
            @RequestHeader("X-User-ID") Long adminId) {
        List<FrequentCustomerDTO> customers = adminService.getFrequentCustomers(adminId);
        return ResponseEntity.ok(customers);
    }

    // GET /api/admin/barbers/leaves - View all barber leave requests
    @GetMapping("/barbers/leaves")
    public ResponseEntity<List<BarberLeaveDTO>> getAllBarberLeaves(
            @RequestHeader("X-User-ID") Long adminId,
            @RequestParam(required = false) LeaveStatus status) {
        List<BarberLeaveDTO> leaves = adminService.getAllBarberLeaves(adminId, status);
        return ResponseEntity.ok(leaves);
    }

    // PUT /api/admin/barbers/{barberId}/leaves/{leaveId}/approve
    @PutMapping("/barbers/{barberId}/leaves/{leaveId}/approve")
    public ResponseEntity<String> approveLeave(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberId,
            @PathVariable Long leaveId) {

        adminService.updateLeaveStatus(leaveId, barberId, LeaveStatus.APPROVED, adminId);
        return ResponseEntity.ok("Leave approved successfully");
    }

    // PUT /api/admin/barbers/{barberId}/leaves/{leaveId}/reject

    @PutMapping("/barbers/{barberId}/leaves/{leaveId}/reject")
    public ResponseEntity<String> rejectLeave(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberId,
            @PathVariable Long leaveId) {

        adminService.updateLeaveStatus(leaveId, barberId, LeaveStatus.REJECTED, adminId);
        return ResponseEntity.ok("Leave rejected successfully");
    }
}
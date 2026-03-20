package com.sijan.barberReservation.controller;


import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.AdminMapper;
import com.sijan.barberReservation.mapper.user.UpdateUserRequestMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final AppointmentService appointmentService;
    private final BarberService barberService;
    private final BarberLeaveService barberLeaveService;
    private final PageMapper pageMapper;

    private Admin getCurrentAdmin(UserPrincipal userPrincipal) {
        return adminService.findById(userPrincipal.getId());
    }

//    @GetMapping("/barbers")
//    public ResponseEntity<PageResponse<BarberDTO>> getBarbersByBarbershop(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @AuthenticationPrincipal UserPrincipal userPrincipal
//    ) {
//        Admin admin = getCurrentAdmin(userPrincipal);
//        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
//        Pageable pageable = PageRequest.of(page, size, sort);
//        Page<Barber> barbers = barberService.findByBarberShop(admin, pageable);
//        return ResponseEntity.ok(pageMapper.toBarberPageResponse(barbers));
//    }

    @GetMapping("/appointment")
    public ResponseEntity<PageResponse<AppointmentDetailsResponse>> getAllAppointment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Admin admin = getCurrentAdmin(userPrincipal);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> result = appointmentService.getAppointmentsForAdmin(admin, pageable);
        return ResponseEntity.ok(pageMapper.toAppointmentPageResponse(result));
    }

    @GetMapping("/main/dashboard")
    @PreAuthorize("hasRole('MAIN_ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboardDetails(
    ) {
        return ResponseEntity.ok(adminService.getDashboardData());
    }

    @GetMapping("/{adminId}/dashboard")
    @PreAuthorize("hasRole('SHOP_ADMIN')")
    public ResponseEntity<ShopAdminDashboardResponse> getShopAdminDashboardDetails(@PathVariable Long adminId
    ) {
        return ResponseEntity.ok(adminService.getShopAdminDashboardData(adminService.findById(adminId)));
    }
    @PutMapping("/{adminId}/update")
    public ResponseEntity<AdminDTO> updateProfile(@PathVariable Long adminId, @RequestBody UpdateUserRequest request) {
        Admin admin = adminService.findById(adminId);
        Admin updated = adminService.update(admin, request.getName(), request.getPhone());
        return ResponseEntity.ok(adminMapper.toDTO(updated));
    }


    @PutMapping("/{adminId}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Long adminId, @RequestBody ChangePasswordRequest request) {
        adminService.changePassword(adminService.findById(adminId), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/leaves")
    public ResponseEntity<PageResponse<BarberLeaveDTO>> getAllBarberLeaves(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Admin admin = getCurrentAdmin(userPrincipal);
        Sort sort = Sort.by(Sort.Direction.DESC, "scheduledTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BarberLeave> leaves = barberService.getAllLeaves(admin, pageable);
        return ResponseEntity.ok(pageMapper.toBarberLeavePageResponse(leaves));
    }

    @PutMapping("/barbers/{barberId}/leaves/{leaveId}/status")
    public ResponseEntity<Void> updateLeaveStatus(
            @PathVariable Long barberId,
            @PathVariable Long leaveId,
            @RequestBody UpdateLeaveStatusRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Admin admin = getCurrentAdmin(userPrincipal);
        BarberLeave leave = barberLeaveService.findById(leaveId);
        Barber barber = barberService.findById(barberId);
        barberLeaveService.updateLeaveStatus(
                leave,
                barber,
                request.getStatus(),
                admin);
        return ResponseEntity.noContent().build();
    }
}
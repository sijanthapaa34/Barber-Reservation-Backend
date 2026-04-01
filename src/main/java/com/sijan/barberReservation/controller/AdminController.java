package com.sijan.barberReservation.controller;


import com.sijan.barberReservation.DTO.Auth.ChangePasswordRequest;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.AdminMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.UpdateUserRequestMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.*;
import jakarta.transaction.Transactional;
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
    private final BarbershopMapper barbershopMapper;

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

    @GetMapping("/{adminId}/shop")
    @PreAuthorize("hasRole('SHOP_ADMIN')")
    @Transactional
    public ResponseEntity<BarbershopDTO> getShopByAdmin(@PathVariable Long adminId) {
        Admin admin = adminService.findById(adminId);
        if (admin.getBarbershop() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(barbershopMapper.toDTO(admin.getBarbershop()));
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
}
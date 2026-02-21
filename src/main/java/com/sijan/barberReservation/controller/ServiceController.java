package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.service.ServiceUpdateRequest;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.AdminService;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service")
public class ServiceController {
    private final ServiceOfferingService serviceOfferingService;
    private final BarbershopService barbershopService;
    private final AdminService adminService;
    private final ServiceMapper serviceMapper;
    private final PageMapper pageMapper;

    public ServiceController(ServiceOfferingService serviceOfferingService, BarbershopService barbershopService, AdminService adminService, ServiceMapper serviceMapper, PageMapper pageMapper) {
        this.serviceOfferingService = serviceOfferingService;
        this.barbershopService = barbershopService;
        this.adminService = adminService;
        this.serviceMapper = serviceMapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<ServiceDTO> findById(
            @PathVariable Long serviceId) {
        ServiceDTO response = serviceMapper.toDTO(serviceOfferingService.findById(serviceId));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<ServiceDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ServiceDTO> response = pageMapper.toServicePageResponse(serviceOfferingService.getAll(pageable));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/barberShop/{barberShopId}")
    public ResponseEntity<PageResponse<ServiceDTO>> getAllByBarbershop( @PathVariable Long barberShopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Barbershop barbershop = barbershopService.findById(barberShopId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ServiceDTO> response = pageMapper.toServicePageResponse(serviceOfferingService.getAllByBarbershop(barbershop,pageable));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ServiceDTO> add(
            @RequestBody RegisterServiceRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Admin admin = adminService.findById(userPrincipal.getId());
        ServiceOffering service = serviceMapper.toEntity(request);
        Barbershop barberShop = admin.getBarbershop();
        ServiceDTO serviceDTO = serviceMapper.toDTO(serviceOfferingService.add(barberShop, service));
        return ResponseEntity.status(201).body(serviceDTO);
    }

    @PutMapping("/services/{serviceId}")
    public ResponseEntity<ServiceDTO> update(
            @PathVariable Long serviceId,
            @RequestBody ServiceUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal

    ) {
        ServiceOffering serviceOffering = serviceOfferingService.findById(serviceId);
        ServiceDTO updated = serviceMapper.toDTO(serviceOfferingService.update(adminService.findById(userPrincipal.getId()), serviceOffering));
        return ResponseEntity.ok(updated);
    }

//    // PUT /api/admin/services/{id}/activate - Reactivate a service
//    @PutMapping("/services/{id}/activate")
//    public ResponseEntity<String> activateService(
//            @PathVariable Long id,
//            Authentication authentication
//    ) {
//        Admin admin = getCurrentAdmin(authentication);
//        serviceOfferingService.activateService(admin, id);
//        return ResponseEntity.ok("Service activated successfully");
//    }
//
//    // PUT /api/admin/services/{id}/deactivate - Deactivate service
//    @PutMapping("/services/{id}/deactivate")
//    public ResponseEntity<String> deactivateService(
//            @PathVariable Long id,
//            Authentication authentication
//    ) {
//        Admin admin = getCurrentAdmin(authentication);
//        adminService.deactivateService(adminId, id);
//        return ResponseEntity.ok("Service deactivated successfully");
//    }
}

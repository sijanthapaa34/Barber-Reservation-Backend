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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceController {
    private final ServiceOfferingService serviceOfferingService;
    private final BarbershopService barbershopService;
    private final AdminService adminService;
    private final ServiceMapper serviceMapper;
    private final PageMapper pageMapper;

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

    @GetMapping("/barbershop/{barbershopId}")
    public ResponseEntity<PageResponse<ServiceDTO>> getAllByBarbershop( @PathVariable Long barbershopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Barbershop barbershop = barbershopService.findById(barbershopId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ServiceDTO> response = pageMapper.toServicePageResponse(serviceOfferingService.getAllByBarbershop(barbershop,pageable));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{barbershopId}")
    public ResponseEntity<ServiceDTO> add(@PathVariable Long barbershopId,
            @RequestBody RegisterServiceRequest request
    ) {
        ServiceOffering service = serviceMapper.toEntity(request);
        Barbershop barberShop = barbershopService.findById(barbershopId);
        ServiceDTO serviceDTO = serviceMapper.toDTO(serviceOfferingService.add(barberShop, service));
        return ResponseEntity.status(201).body(serviceDTO);
    }

    @PatchMapping("/{barbershopId}/update/{serviceId}")
    public ResponseEntity<ServiceDTO> update(
            @PathVariable Long serviceId,
            @PathVariable Long barbershopId,
            @RequestBody ServiceUpdateRequest request
    ) {
        ServiceOffering service = serviceOfferingService.findById(serviceId);
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This service does not belong to the specified shop.");
        }
        ServiceDTO updated = serviceMapper.toDTO(serviceOfferingService.update(service, request.getDurationMinutes(), request.getPrice(), request.getServiceImages()));
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{barbershopId}/activate/{serviceId}")
    public ResponseEntity<Void> activateService(
            @PathVariable Long barbershopId,
            @PathVariable Long serviceId
    ) {
        ServiceOffering service = serviceOfferingService.findById(serviceId);
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This service does not belong to the specified shop.");
        }
        serviceOfferingService.activateService(service);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{barbershopId}/deactivate/{serviceId}")
    public ResponseEntity<String> deactivateService(
            @PathVariable Long barbershopId,
            @PathVariable Long serviceId
    ) {
        ServiceOffering service = serviceOfferingService.findById(serviceId);
        if (!service.getBarbershop().getId().equals(barbershopId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This service does not belong to the specified shop.");
        }
        serviceOfferingService.deactivateService(service);
        return ResponseEntity.ok().build();
    }
}

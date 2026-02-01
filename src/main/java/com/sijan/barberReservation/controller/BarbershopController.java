package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.service.RegisterServiceRequest;
import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarberShopMapper;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarberShopService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/barbershop")
public class BarbershopController {

    private final BarberShopService barberShopService;
    private final BarberService barberService;
    private final ServiceOfferingService serviceOfferingService;
    private final BarberShopMapper barberShopMapper;
    private final ServiceMapper serviceMapper;
    private final BarberMapper barberMapper;


    public BarbershopController(BarberShopService barberShopService, BarberService barberService, ServiceOfferingService serviceOfferingService, BarberShopMapper barberShopMapper, ServiceMapper serviceMapper, BarberMapper barberMapper) {
        this.barberShopService = barberShopService;
        this.barberService = barberService;
        this.serviceOfferingService = serviceOfferingService;
        this.barberShopMapper = barberShopMapper;
        this.serviceMapper = serviceMapper;
        this.barberMapper = barberMapper;
    }

//    @PostMapping
//    public ResponseEntity<BarberShopDTO> createBarbershop(
//            @RequestHeader("X-User-ID") Long adminId,
//            @RequestBody @Valid RegisterBarberShopRequest request) {
//        BarberShop barberShop = barberShopMapper.toEntity(request);
//        BarberShopDTO barbershopDTO = barberShopMapper.toDTO(barberShopService.register(adminId, barberShop));
//        return ResponseEntity.ok(barbershopDTO);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberShopDTO> getBarbershop(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long id) {
        BarberShop barbershop = barberShopService.findById(id);
        return ResponseEntity.ok(barberShopMapper.toDTO(barbershop));
    }

    // Update barbershop details
    @PutMapping("/{id}")
    public ResponseEntity<BarberShopDTO> updateBarbershop(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberShopId,
            @RequestBody @Valid UpdateBarberShopRequest request) {
        BarberShop barberShop = barberShopMapper.toEntity(request);
        BarberShop barbershop = barberShopService.updateBarbershop(adminId, barberShopId, barberShop);
        return ResponseEntity.ok(barberShopMapper.toDTO(barbershop));
    }



    // Get all services for a barbershop
//    @GetMapping("/{barbershopId}/services")
//    public ResponseEntity<List<ServiceDTO>> getAllServices(
//            @RequestHeader("X-User-ID") Long adminId,
//            @PathVariable Long barbershopId) {
//        List<ServiceOffering> services = serviceOfferingService.getAllServices(adminId, barbershopId);
//        return ResponseEntity.ok(services);
//    }



    // Add a new service to a barbershop
//    @PostMapping("/{barbershopId}/services")
//    public ResponseEntity<ServiceDTO> addService(
//            @RequestHeader("X-User-ID") Long adminId,
//            @PathVariable Long barbershopId,
//            @RequestBody @Valid RegisterServiceRequest request) {
//        ServiceOffering service = serviceOfferingService.addService(adminId, barbershopId, request);
//        return ResponseEntity.status(201).body(service);
//    }
//
//    // Update a barber
//    @PutMapping("/{barbershopId}/barbers/{barberId}")
//    public ResponseEntity<BarberDTO> updateBarber(
//            @RequestHeader("X-User-ID") Long adminId,
//            @PathVariable Long barbershopId,
//            @PathVariable Long barberId,
//            @RequestBody @Valid UpdateBarberRequest request) {
//        BarberDTO barber = barberShopService.updateBarber(adminId, barbershopId, barberId, request);
//        return ResponseEntity.ok(barber);
//    }

    // Update a service
//    @PutMapping("/{barbershopId}/services/{serviceId}")
//    public ResponseEntity<ServiceDTO> updateService(
//            @RequestHeader("X-User-ID") Long adminId,
//            @PathVariable Long barbershopId,
//            @PathVariable Long serviceId,
//            @RequestBody @Valid UpdateServiceRequest request) {
//        ServiceOfferingDTO service = barberShopService.updateService(adminId, barbershopId, serviceId, request);
//        return ResponseEntity.ok(service);
//    }

    // Find nearby barbershops
//    @GetMapping("/nearby")
//    public ResponseEntity<List<BarberShopDTO>> findNearbyBarbershops(
//            @RequestParam Double latitude,
//            @RequestParam Double longitude,
//            @RequestParam(defaultValue = "5.0") Double radiusKm) {
//        List<BarberShopDTO> barbershops = barberShopService.findNearbyBarbershops(
//                latitude, longitude, radiusKm);
//        return ResponseEntity.ok(barbershops);
//    }
}
package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.service.ServiceMapper;
import com.sijan.barberReservation.mapper.user.BarberMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barbershop")
public class BarbershopController {

    private final BarbershopService barbershopService;
    private final BarberService barberService;
    private final ServiceOfferingService serviceOfferingService;
    private final BarbershopMapper barbershopMapper;
    private final ServiceMapper serviceMapper;
    private final BarberMapper barberMapper;


    public BarbershopController(BarbershopService barbershopService, BarberService barberService, ServiceOfferingService serviceOfferingService, BarbershopMapper barbershopMapper, ServiceMapper serviceMapper, BarberMapper barberMapper) {
        this.barbershopService = barbershopService;
        this.barberService = barberService;
        this.serviceOfferingService = serviceOfferingService;
        this.barbershopMapper = barbershopMapper;
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
    public ResponseEntity<BarbershopDTO> getBarbershop(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long id) {
        BarberShop barbershop = barbershopService.findById(id);
        return ResponseEntity.ok(barbershopMapper.toDTO(barbershop));
    }

    // Update barbershop details
    @PutMapping("/{id}")
    public ResponseEntity<BarbershopDTO> updateBarbershop(
            @RequestHeader("X-User-ID") Long adminId,
            @PathVariable Long barberShopId,
            @RequestBody @Valid UpdateBarbershopRequest request) {
        BarberShop barberShop = barbershopMapper.toEntity(request);
        BarberShop barbershop = barbershopService.updateBarbershop(adminId, barberShopId, barberShop);
        return ResponseEntity.ok(barbershopMapper.toDTO(barbershop));
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
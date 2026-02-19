package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.user.BarbershopMapper;
import com.sijan.barberReservation.mapper.user.UpdateBarbershopRequestMapper;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.UserPrincipal;
import com.sijan.barberReservation.service.AdminService;
import com.sijan.barberReservation.service.BarbershopService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barbershop")
public class BarbershopController {

    private final BarbershopService barbershopService;
    private final AdminService adminService;
    private final PageMapper pageMapper;
    private final BarbershopMapper barbershopMapper;
    private final UpdateBarbershopRequestMapper updateBarbershopRequestMapper;


    public BarbershopController(BarbershopService barbershopService, AdminService adminService, PageMapper pageMapper, BarbershopMapper barbershopMapper, UpdateBarbershopRequestMapper updateBarbershopRequestMapper) {
        this.barbershopService = barbershopService;
        this.adminService = adminService;
        this.pageMapper = pageMapper;
        this.barbershopMapper = barbershopMapper;
        this.updateBarbershopRequestMapper = updateBarbershopRequestMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarbershopDTO> findById(
            @PathVariable Long id) {
        Barbershop barbershop = barbershopService.findById(id);
        return ResponseEntity.ok(barbershopMapper.toDTO(barbershop));
    }

    // Update barbershop details
    @PutMapping("/{barbershopId}")
    @PreAuthorize("hasRole('ADMIN') or @barbershopSecurity.isOwner(authentication, #id)")
    public ResponseEntity<BarbershopDTO> update(
            @PathVariable Long barbershopId,
            @RequestBody @Valid UpdateBarbershopRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Barbershop barbershop = barbershopService.findById(barbershopId);
        updateBarbershopRequestMapper.toEntity(barbershop, request);
        Barbershop updatedShop = barbershopService.update(updateBarbershopRequestMapper.toEntity(barbershop, request), adminService.findById(userPrincipal.getId()));
        return ResponseEntity.ok(barbershopMapper.toDTO(updatedShop));
    }

    @GetMapping("/nearby")
    public ResponseEntity<PageResponse<BarbershopDTO>> findNearby(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radiusKm) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Barbershop> shops = barbershopService.findNearby(
                latitude, longitude, radiusKm, pageable);
        return ResponseEntity.ok(pageMapper.toBarbershopPageResponse(shops));
    }

    @GetMapping("search/{word}")
    public ResponseEntity<PageResponse<BarbershopDTO>> searchByWord(@PathVariable String word,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Barbershop> shops = barbershopService.searchByWord(word, pageable);
        return ResponseEntity.ok(pageMapper.toBarbershopPageResponse(shops));
    }
    @GetMapping("/top-rated")
    public ResponseEntity<PageResponse<BarbershopDTO>> getTopRated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("rating").descending());
        Page<Barbershop> shops = barbershopService.findTopRated(pageable);
        return ResponseEntity.ok(pageMapper.toBarbershopPageResponse(shops));
    }
}
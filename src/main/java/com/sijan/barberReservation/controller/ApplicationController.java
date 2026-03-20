package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.application.ApplicationRequest;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.service.ApplicationService;
import com.sijan.barberReservation.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final OtpService otpService;
    private final ApplicationMapper applicationMapper;
    private final PageMapper pageMapper;

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailResponse> findById(@PathVariable Long applicationId){
        Application application = applicationService.findById(applicationId);
        return ResponseEntity.ok(applicationMapper.toDTO(application));
    }

    @GetMapping("/main-admin")
    public ResponseEntity<PageResponse<ApplicationDetailResponse>> getForMainAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> application = applicationService.getRelevantForMainAdmin(pageable);
        application.forEach(application1 -> System.out.println(application1.getType() + application1.getBarbershopName()));
        return ResponseEntity.ok(pageMapper.toApplicationPageResponse(application));
    }

    @GetMapping("/shop/{barbershopId}")
    public ResponseEntity<PageResponse<ApplicationDetailResponse>> getAllByBarbershop(
            @PathVariable Long barbershopId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> application = applicationService.getPendingForShopAdmin(barbershopId, pageable);
        return ResponseEntity.ok(pageMapper.toApplicationPageResponse(application));
    }


    @PostMapping
    public ResponseEntity<ApplicationDetailResponse> submitApplication(@RequestBody ApplicationRequest request) {
        boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP.");
        }
        Application application = applicationMapper.toEntity(request);
        Application savedApplication = applicationService.save(application);
        return ResponseEntity.ok(applicationMapper.toDTO(savedApplication));
    }

    @PatchMapping("/{applicationId}/shop-approve")
    public ResponseEntity<Void> approveByShopAdmin(@PathVariable Long applicationId){
        applicationService.approveByShopAdmin(applicationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<Void> approveByMainAdmin(@PathVariable Long applicationId){
        applicationService.approveByMainAdmin(applicationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long applicationId){
        applicationService.reject(applicationService.findById(applicationId));
        return ResponseEntity.noContent().build();
    }
}
package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.application.ApplicationRequest;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;
    private final PageMapper pageMapper;

    public ApplicationController(ApplicationService applicationService, ApplicationMapper applicationMapper, PageMapper pageMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
        this.pageMapper = pageMapper;
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailResponse> findById(@PathVariable Long applicationId){
        Application application = applicationService.findById(applicationId);
        return ResponseEntity.ok(applicationMapper.toDTO(application));
    }
    @GetMapping
    public ResponseEntity<PageResponse<ApplicationDetailResponse>> getAll(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> application = applicationService.getAll(pageable);
        return ResponseEntity.ok(pageMapper.toApplicationPageResponse(application));
    }
    @PostMapping
    public ResponseEntity<ApplicationDetailResponse> submitApplication(
           @RequestBody ApplicationRequest request
    ) {
        System.out.println(request.getEmail() + request.getType());
        Application application = applicationMapper.toEntity(request);
        Application savedApplication = applicationService.save(application);
        return ResponseEntity.ok(applicationMapper.toDTO(savedApplication));
    }
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long applicationId){
        applicationService.approve(applicationService.findById(applicationId));
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long applicationId){
        applicationService.reject(applicationService.findById(applicationId));
        return ResponseEntity.noContent().build();
    }
}

package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.application.ApplicationDetailResponse;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.RescheduleAppointmentRequest;
import com.sijan.barberReservation.mapper.application.ApplicationMapper;
import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    public ApplicationController(ApplicationService applicationService, ApplicationMapper applicationMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailResponse> findById(@PathVariable Long applicationId){
        Application application = applicationService.findById(applicationId);
        return ResponseEntity.ok(applicationMapper.toDTO(application));
    }


}

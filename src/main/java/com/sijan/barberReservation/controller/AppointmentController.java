package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.mapper.appointment.AppointmentSlotMapper;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.CreateMapper;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.service.AppointmentService;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarberShopService;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController{

    private final AppointmentService appointmentService;
    private final AppointmentDetailsMapper appointmentDetailsMapper;
    private final CreateMapper createAppointmentMapper;
    private final PageMapper pageMapper;
    private final BarberService barberService;
    private final BarberShopService barberShopService;
    private final CustomerService customerService;
    private final ServiceOfferingService serviceOfferingService;
    private final AppointmentSlotMapper appointmentSlotMapper;


    public AppointmentController(AppointmentService appointmentService, AppointmentDetailsMapper appointmentDetailsMapper, AppointmentDetailsMapper appointmentDetailsMapper1, CreateMapper createAppointmentMapper, PageMapper pageMapper, BarberService barberService, BarberShopService barberShopService, CustomerService customerService, ServiceOfferingService serviceOfferingService, AppointmentSlotMapper appointmentSlotMapper) {
        this.appointmentService = appointmentService;
        this.appointmentDetailsMapper = appointmentDetailsMapper;
        this.createAppointmentMapper = createAppointmentMapper;
        this.pageMapper = pageMapper;
        this.barberService = barberService;
        this.barberShopService = barberShopService;
        this.customerService = customerService;
        this.serviceOfferingService = serviceOfferingService;
        this.appointmentSlotMapper = appointmentSlotMapper;
    }
    @GetMapping("/{appointmentId}")
    public ResponseEntity<Appointment> findById(Long appointmentId){
        Appointment appointment = appointmentService.findById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

    @PostMapping
    public ResponseEntity<DetailsDTO> book(
            @RequestBody CreateAppointmentRequest request) {
        Appointment appointment = createAppointmentMapper.toAppointment(request);
        DetailsDTO booked = appointmentDetailsMapper.toDetailsDTO(appointmentService.book(appointment));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(booked);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<DetailsDTO> viewAppointmentDetails(
            @PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.findById(appointmentId);
        DetailsDTO details = appointmentDetailsMapper.toDetailsDTO(appointmentService.viewDetails(appointment));
        return ResponseEntity.ok(details);
    }
    @GetMapping("/customer/{customerId}/upcoming")
    public ResponseEntity<PageResponse<DetailsDTO>> upcoming(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Customer customer = customerService.findById(customerId);

        Page<Appointment> result =
                appointmentService.getUpcoming(customer, page, size);
        PageResponse<DetailsDTO> pageResponse= pageMapper.toPageResponse(result);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/customer/{customerId}/past")
    public ResponseEntity<PageResponse<DetailsDTO>> past(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Customer customer = customerService.findById(customerId);

        Page<Appointment> result =
                appointmentService.getPast(customer, page, size);
        PageResponse<DetailsDTO> pageResponse= pageMapper.toPageResponse(result);
        return ResponseEntity.ok(pageResponse);
    }


    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelAppointment(
            @PathVariable Long appointmentId) {
        Appointment appointment = appointmentService.findById(appointmentId);
        String result = appointmentService.cancelAppointment(appointment);
        return ResponseEntity.ok(result);
    }
    // Reschedule an appointment
//    @PutMapping("/{id}/reschedule")
//    public ResponseEntity<DetailsDTO> reschedule(
//            @PathVariable Long id) {
//
//        DetailsDTO rescheduled = toappointmentService.rescheduleAppointment(id, email, request);
//        return ResponseEntity.ok( rescheduled);
//    }
    @GetMapping("/admin")
    public ResponseEntity<List<DetailsDTO>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Appointment> appointments = appointmentService.getAllAppointments(page, size);
        List<DetailsDTO> details= appointmentDetailsMapper.toDetailsDTO((List<Appointment>) appointments);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/me/availability")
    public ResponseEntity<AvailableSlotsResponseDTO> getAvailableSlots(
            @RequestParam Long barberId,
            @RequestParam List<Long> serviceIds,
            @RequestParam LocalDate date
    ) {
        Barber barber = barberService.findById(barberId);

        List<ServiceOffering> services = serviceIds.stream()
                .map(serviceOfferingService::findById)
                .toList();
        int totalDurationMinutes = services.stream()
                .mapToInt(ServiceOffering::getDurationMinutes)
                .sum();
        List<LocalDateTime> availableSlotTimes =
                appointmentService.getAvailableSlotsEntity(
                        barber,
                        date,
                        totalDurationMinutes
                );
        List<Appointment> bookedAppointments =
                appointmentService.getBookedAppointments(barber, date);

        List<TimeSlotDTO> availableSlots =
                appointmentSlotMapper.toAvailableSlots(
                        availableSlotTimes,
                        totalDurationMinutes
                );

        List<TimeSlotDTO> bookedSlots =
                appointmentSlotMapper.toTimeSlotDTOList(bookedAppointments);

        AvailableSlotsResponseDTO response =
                appointmentSlotMapper.toAvailableSlotsResponse(
                        barber,
                        services,
                        date,
                        availableSlots,
                        bookedSlots
                );

        return ResponseEntity.ok(response);
    }


}


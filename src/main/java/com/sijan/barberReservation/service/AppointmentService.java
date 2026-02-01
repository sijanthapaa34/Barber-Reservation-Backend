package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AppointmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerService customerService;
    private final BarberShopService barberShopService;
    private final ServiceOfferingService serviceOfferingService;
    private final BarberService barberService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerService customerService,
            BarberShopService barberShopService, ServiceOfferingService serviceOfferingService,
            BarberService barberService) {
        this.appointmentRepository = appointmentRepository;
        this.customerService = customerService;
        this.barberShopService = barberShopService;
        this.serviceOfferingService = serviceOfferingService;
        this.barberService = barberService;
    }
    public Appointment findById(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public Appointment book(Appointment appointment) {
        if (appointment.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book an appointment in the past");
        }

        double totalPrice = appointment.getServices().stream().mapToDouble(ServiceOffering::getPrice).sum();
        int totalDuration = appointment.getServices().stream().mapToInt(ServiceOffering::getDurationMinutes).sum();
        appointment.setTotalPrice(totalPrice);
        appointment.setTotalDurationMinutes(totalDuration);

        return appointmentRepository.save(appointment);
    }


    public Appointment viewDetails(Appointment appointment) {
       return appointment;
    }

    public String cancelAppointment(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "Appointment is already canceled";
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return "Completed appointments cannot be canceled";
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        return "Cancelled Successfully";
    }

    public Appointment reschedule(Long id, String email, RescheduleAppointmentRequest request) {
        Appointment oldAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("xAppointment not found"));
        if (!oldAppointment.getCustomer().getEmail().equals(email) || !oldAppointment.getBarber().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }

        LocalDateTime appointmentDateTime = request.getNewDateTime();

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book an appointment in the past");
        }

        LocalDateTime checkInTime = appointmentDateTime.minusMinutes(10);

        oldAppointment.setCheckInTime(checkInTime);
        oldAppointment.setScheduledTime(appointmentDateTime);
        oldAppointment.setCheckInTime(checkInTime);

        AppointmentLog log = new AppointmentLog();
        log.setAppointment(oldAppointment);
//        log.setAction(AppointmentStatus.RESCHEDULED);
        log.setDescription(request.getReason());
        log.setPerformedBy(oldAppointment.getCustomer().getName());

        return appointmentRepository.save(oldAppointment);

    }


    public Page<Appointment> getUpcoming(Customer customer, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());

        return appointmentRepository.findUpcomingByCustomer(
                customer,
                LocalDate.now(),
                pageable
        );
    }

    public Page<Appointment> getPast(Customer customer, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").descending());

        return appointmentRepository.findPastByCustomer(
                customer,
                LocalDate.now(),
                pageable
        );
    }
    public List<LocalDateTime> getAvailableSlotsEntity(
            Barber barber,
            LocalDate date,
            int requestedDurationMinutes
    ) {
        List<Appointment> bookedAppointments =
                getBookedAppointments(barber, date);
        List<LocalTime> candidateSlots =
                generateSlots(barber, requestedDurationMinutes, date);
        return candidateSlots.stream()
                .filter(slot ->
                        isSlotAvailable(
                                slot,
                                requestedDurationMinutes,
                                bookedAppointments
                        )
                )
                .map(time -> LocalDateTime.of(date, time))
                .toList();
    }

    public List<Appointment> getBookedAppointments(
            Barber barber,
            LocalDate date
    ) {
        return appointmentRepository
                .findByBarberAndScheduledTimeBetweenAndStatusIn(
                        barber,
                        date.atStartOfDay(),
                        date.atTime(23, 59, 59),
                        List.of(
                                AppointmentStatus.SCHEDULED
                        )
                );
    }

    private boolean isSlotAvailable(
            LocalTime slotStart,
            int requestedDurationMinutes,
            List<Appointment> bookedAppointments
    ) {
        LocalTime slotEnd = slotStart.plusMinutes(requestedDurationMinutes);

        for (Appointment appointment : bookedAppointments) {
            LocalTime bookedStart = appointment.getStartTime();
            LocalTime bookedEnd = appointment.getEndTime();

            boolean overlaps =
                    slotStart.isBefore(bookedEnd)
                            && slotEnd.isAfter(bookedStart);

            if (overlaps) return false;
        }

        return true;
    }

    private List<LocalTime> generateSlots(
            Barber barber,
            int durationMinutes,
            LocalDate date
    ) {
        DayOfWeek day = date.getDayOfWeek();

        BarberSchedule schedule = barber.getSchedules().stream()
                .filter(s -> s.getDay() == day)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Barber not working on this day")
                );

        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = schedule.getStartTime();
        LocalTime end = schedule.getEndTime();

        while (!current.plusMinutes(durationMinutes).isAfter(end)) {
            slots.add(current);
            current = current.plusMinutes(15); // ⬅️ slot interval
        }

        return slots;
    }


    public List<Appointment> getBarberAppointments(Barber barber, LocalDate targetDate) {
        return appointmentRepository.findByBarberAndScheduledTime(barber, targetDate);
    }

    public Page<Appointment> getAllAppointments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledTime").descending());
        return appointmentRepository.findAll(pageable);
    }

}

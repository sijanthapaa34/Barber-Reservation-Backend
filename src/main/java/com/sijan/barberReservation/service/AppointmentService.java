package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.exception.appointment.AppointmentAlreadyCancelledException;
import com.sijan.barberReservation.exception.appointment.AppointmentNotFoundException;
import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.exception.appointment.InvalidAppointmentTimeException;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AppointmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AdminService adminService;

    public AppointmentService(AppointmentRepository appointmentRepository, AdminService adminService) {
        this.appointmentRepository = appointmentRepository;
        this.adminService = adminService;
    }

    @Transactional
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Transactional
    public Appointment book(Appointment appointment, Customer customer) {


        int totalDurationMinutes = appointment.getServices().stream()
                .mapToInt(ServiceOffering::getDurationMinutes)
                .sum();

        double totalPrice = appointment.getServices().stream()
                .mapToDouble(ServiceOffering::getPrice)
                .sum();

        appointment.setTotalDurationMinutes(totalDurationMinutes);
        appointment.setTotalPrice(totalPrice);

        if (appointment.getScheduledTime().getMinute() % 30 != 0) {
            throw new InvalidAppointmentTimeException("Appointments must start at 30-min intervals");
        }

        List<LocalDateTime> availableSlots =
                computeAvailableSlots(appointment.getBarber(), appointment.getScheduledTime().toLocalDate(), appointment.getServices());

        if (!availableSlots.contains(appointment.getScheduledTime())) {
            throw new AppointmentSlotUnavailableException("Selected slot is not available");
        }

        appointment.setCustomer(customer);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPaymentStatus(PaymentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Page<Appointment> getUpcoming(Customer customer, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findUpcomingByCustomer(
                customer,
                now,
                PageRequest.of(page, size)
        );
    }

    @Transactional
    public Page<Appointment> getPast(Customer customer, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findPastByCustomer(
                customer,
                now,
                PageRequest.of(page, size)
        );
    }

    @Transactional
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = findById(appointmentId);
        if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
            throw new AppointmentAlreadyCancelledException(appointmentId);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public List<LocalDateTime> computeAvailableSlots(Barber barber, LocalDate date, List<ServiceOffering> services) {
        int totalDurationMinutes = services.stream()
                .mapToInt(ServiceOffering::getDurationMinutes)
                .sum();

        int slotIncrement = 30;
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd   = date.atTime(LocalTime.MAX);

        List<Appointment> bookedAppointments =
                appointmentRepository.findByBarberAndScheduledTimeBetween(
                        barber,
                        dayStart,
                        dayEnd
                );

        LocalDateTime workStart = date.atTime(9, 0);
        LocalDateTime workEnd = date.atTime(18, 0);

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime slotStart = workStart;

        while (!slotStart.plusMinutes(totalDurationMinutes).isAfter(workEnd)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(totalDurationMinutes);

            LocalDateTime finalSlotStart = slotStart;
            boolean conflict = bookedAppointments.stream().anyMatch(a -> {
                LocalDateTime existingStart = a.getScheduledTime();
                LocalDateTime existingEnd = existingStart.plusMinutes(a.getTotalDurationMinutes());
                return finalSlotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart);
            });

            if (!conflict) {
                availableSlots.add(slotStart);
            }

            slotStart = slotStart.plusMinutes(slotIncrement);
        }

        return availableSlots;
    }

    @Transactional
    public List<Appointment> getBookedAppointments(Barber barber, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
                barber,
                AppointmentStatus.SCHEDULED,
                startOfDay,
                endOfDay
        );
    }

    @Transactional
    public List<Appointment> getBarberAppointments(Barber barber, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findByBarberAndScheduledTimeBetween(
                barber,
                startOfDay,
                endOfDay
        );
    }

    public Page<Appointment> getAppointmentsForAdmin(Admin admin, Pageable pageable) {
        if (admin.getRole() == Roles.MAIN_ADMIN) {
            return appointmentRepository.findAll(pageable);

        } else if (admin.getRole() == Roles.SHOP_ADMIN) {
            return appointmentRepository.findAllByBarbershop(admin.getBarbershop(), pageable);

        } else {
            throw new AccessDeniedException("Invalid role for this action");
        }
    }
}


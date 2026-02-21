package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.exception.appointment.AppointmentAlreadyCancelledException;
import com.sijan.barberReservation.exception.appointment.AppointmentNotFoundException;
import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.exception.appointment.InvalidAppointmentTimeException;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.mapper.appointment.AppointmentSlotMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AppointmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotMapper appointmentSlotMapper;

    public AppointmentService(AppointmentRepository appointmentRepository, AppointmentSlotMapper appointmentSlotMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentSlotMapper = appointmentSlotMapper;
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
                computeAvailableSlots(appointment.getBarber(), appointment.getScheduledTime().toLocalDate(), appointment.getServices(),null);

        if (!availableSlots.contains(appointment.getScheduledTime())) {
            throw new AppointmentSlotUnavailableException("Selected slot is not available");
        }
        LocalDateTime checkInTime = appointment.getScheduledTime().minusMinutes(10);
        appointment.setCheckInTime(checkInTime);
        appointment.setCustomer(customer);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setPaymentStatus(PaymentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    public Page<Appointment> getUpcoming(Customer customer, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findUpcomingByCustomer(
                customer,
                now,
                PageRequest.of(page, size)
        );
    }

    public Page<Appointment> getPast(Customer customer, int page, int size) {
        LocalDateTime now = LocalDateTime.now();

        return appointmentRepository.findPastByCustomer(
                customer,
                now,
                PageRequest.of(page, size)
        );
    }

    @Transactional
    public void cancel(Long appointmentId) {
        Appointment appointment = findById(appointmentId);
        if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
            throw new AppointmentAlreadyCancelledException(appointmentId);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public List<LocalDateTime> computeAvailableSlots(
            Barber barber,
            LocalDate date,
            List<ServiceOffering> services,
            Long excludeAppointmentId // null when booking, appointmentId when rescheduling
    ) {

        int totalDurationMinutes = services.stream()
                .mapToInt(ServiceOffering::getDurationMinutes)
                .sum();

        int slotIncrement = 30;

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);

        // Only consider scheduled appointments (ignore cancelled)
        List<Appointment> bookedAppointments =
                appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
                        barber,
                        AppointmentStatus.SCHEDULED,
                        startOfDay,
                        endOfDay
                );

        LocalDateTime workStart = date.atTime(9, 0);
        LocalDateTime workEnd   = date.atTime(18, 0);

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime slotStart = workStart;

        while (!slotStart.plusMinutes(totalDurationMinutes).isAfter(workEnd)) {

            LocalDateTime slotEnd = slotStart.plusMinutes(totalDurationMinutes);

            LocalDateTime finalSlotStart = slotStart;
            boolean conflict = bookedAppointments.stream()
                    .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                    .anyMatch(existing -> {

                        LocalDateTime existingStart = existing.getScheduledTime();
                        LocalDateTime existingEnd = existingStart.plusMinutes(existing.getTotalDurationMinutes());

                        return finalSlotStart.isBefore(existingEnd)
                                && slotEnd.isAfter(existingStart);
                    });

            if (!conflict) {
                availableSlots.add(slotStart);
            }

            slotStart = slotStart.plusMinutes(slotIncrement);
        }

        return availableSlots;
    }

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

    public AvailableSlotsResponseDTO getAvailability(Barber barber, List<ServiceOffering> services, @NotNull LocalDate date) {
        List<LocalDateTime> availableSlotTimes = computeAvailableSlots(barber, date, services,null);

        List<Appointment> bookedAppointments = getBookedAppointments(barber, date);

        List<TimeSlotDTO> availableSlots = appointmentSlotMapper.toAvailableSlots(
                availableSlotTimes,
                services.stream().mapToInt(ServiceOffering::getDurationMinutes).sum()
        );

        List<TimeSlotDTO> bookedSlots = appointmentSlotMapper.toTimeSlotDTOList(bookedAppointments);

        return  appointmentSlotMapper.toAvailableSlotsResponse(
                barber,
                services,
                date,
                availableSlots,
                bookedSlots
        );
    }

    @Transactional
    public Appointment reschedule(Appointment appointment, LocalDateTime newDateTime) {

        if (newDateTime == null) {
            throw new IllegalArgumentException("New date time cannot be null");
        }

        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot reschedule to past time");
        }

        if (appointment.isCompleted()) {
            throw new IllegalStateException("Completed appointment cannot be rescheduled");
        }

        if (appointment.isCancelled()) {
            throw new IllegalStateException("Cancelled appointment cannot be rescheduled");
        }

        if (appointment.getScheduledTime().equals(newDateTime)) {
            return appointment; // no change
        }

        validateSlotAvailability(appointment, newDateTime);

        appointment.setScheduledTime(newDateTime);
        appointment.setCheckInTime(newDateTime.minusMinutes(10));
        appointment.setCompletedTime(newDateTime.plusMinutes(appointment.getTotalDurationMinutes()));

        return appointment;
    }

    private void validateSlotAvailability(Appointment appointment, LocalDateTime newDateTime) {

        if (newDateTime.getMinute() % 30 != 0) {
            throw new InvalidAppointmentTimeException("Appointments must start at 30-min intervals");
        }

        List<LocalDateTime> availableSlots =
                computeAvailableSlots(
                        appointment.getBarber(),
                        newDateTime.toLocalDate(),
                        appointment.getServices(),
                        appointment.getId()
                );

        if (!availableSlots.contains(newDateTime)) {
            throw new AppointmentSlotUnavailableException("Selected slot is not available");
        }
    }
}


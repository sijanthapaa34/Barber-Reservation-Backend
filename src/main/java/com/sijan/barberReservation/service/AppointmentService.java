//package com.sijan.barberReservation.service;
//
//import com.sijan.barberReservation.DTO.appointment.*;
//import com.sijan.barberReservation.exception.appointment.AppointmentAlreadyCancelledException;
//import com.sijan.barberReservation.exception.appointment.AppointmentNotFoundException;
//import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
//import com.sijan.barberReservation.exception.appointment.InvalidAppointmentTimeException;
//import com.sijan.barberReservation.exception.role.AccessDeniedException;
//import com.sijan.barberReservation.mapper.appointment.AppointmentSlotMapper;
//import com.sijan.barberReservation.model.*;
//import com.sijan.barberReservation.repository.AppointmentRepository;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import com.sijan.barberReservation.repository.BarberLeaveRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//
//@Service
//@RequiredArgsConstructor
//public class AppointmentService {
//
//    private final AppointmentRepository appointmentRepository;
//    private final AppointmentSlotMapper appointmentSlotMapper;
//    private final EmailService emailService;
//    private final BarberLeaveRepository barberLeaveRepository;
//
//    @Transactional
//    public Appointment findById(Long id) {
//        return appointmentRepository.findById(id)
//                .orElseThrow(() -> new AppointmentNotFoundException(id));
//    }
//
//    @Transactional
//    public Appointment book(Appointment appointment, Customer customer) {
//        int totalDurationMinutes = appointment.getServices().stream()
//                .mapToInt(ServiceOffering::getDurationMinutes)
//                .sum();
//
//        double totalPrice = appointment.getServices().stream()
//                .mapToDouble(ServiceOffering::getPrice)
//                .sum();
//
//        appointment.setTotalDurationMinutes(totalDurationMinutes);
//        appointment.setTotalPrice(totalPrice);
//
//        if (appointment.getScheduledTime().getMinute() % 30 != 0) {
//            throw new InvalidAppointmentTimeException("Appointments must start at 30-min intervals");
//        }
//
//        List<LocalDateTime> availableSlots =
//                computeAvailableSlots(appointment.getBarber(), appointment.getScheduledTime().toLocalDate(), appointment.getServices(), null);
//
//        if (!availableSlots.contains(appointment.getScheduledTime())) {
//            throw new AppointmentSlotUnavailableException("Selected slot is not available");
//        }
//
//        LocalDateTime checkInTime = appointment.getScheduledTime().minusMinutes(10);
//        appointment.setCheckInTime(checkInTime);
//        appointment.setCustomer(customer);
//        appointment.setStatus(AppointmentStatus.SCHEDULED);
////        appointment.setPaymentStatus(PaymentStatus.PENDING);
//
//        // Save first to ensure ID is generated and relations are stable
//        Appointment savedAppointment = appointmentRepository.save(appointment);
//
//        // --- SEND EMAILS ---
//        // Extract data needed for email to avoid LazyInitializationException in async thread
//        String customerEmail = customer.getEmail();
//        String customerName = customer.getName();
//        String barberEmail = savedAppointment.getBarber().getEmail();
//        String barberName = savedAppointment.getBarber().getName();
//        String shopName = savedAppointment.getBarber().getBarbershop().getName();
//        String serviceNames = savedAppointment.getServices().stream()
//                .map(ServiceOffering::getName)
//                .collect(Collectors.joining(", "));
//        String date = savedAppointment.getScheduledTime().toLocalDate().toString();
//        String time = savedAppointment.getScheduledTime().toLocalTime().toString();
//
//        // 1. Confirm to Customer
//        emailService.sendAppointmentConfirmationCustomer(
//                customerEmail, customerName, barberName, serviceNames, date, time, shopName
//        );
//
//        // 2. Alert to Barber
//        emailService.sendNewBookingAlert(
//                barberEmail, barberName, customerName, serviceNames, date, time
//        );
//
//        return savedAppointment;
//    }
//
//    public Page<Appointment> getUpcomingByCustomer(Customer customer, int page, int size) {
//        return appointmentRepository.findUpcomingByCustomer(
//                customer,
//                LocalDateTime.now(),
//                PageRequest.of(page, size)
//        );
//    }
//
//    public Page<Appointment> getPastByCustomer(Customer customer, int page, int size) {
//        return appointmentRepository.findPastByCustomer(
//                customer,
//                LocalDateTime.now(),
//                PageRequest.of(page, size)
//        );
//    }
//    public Page<Appointment> getUpcomingByBarber(Barber barber, int page, int size) {
//        return appointmentRepository.findUpcomingByBarber(
//                barber,
//                LocalDateTime.now(),
//                PageRequest.of(page, size)
//        );
//    }
//
//    public Page<Appointment> getPastByBarber(Barber barber, int page, int size) {
//        return appointmentRepository.findPastByBarber(
//                barber,
//                LocalDateTime.now(),
//                PageRequest.of(page, size)
//        );
//    }
//
//    @Transactional
//    public void cancel(Long appointmentId, String cancelledByName) { // Added cancelledByName parameter
//        Appointment appointment = findById(appointmentId);
//        if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
//            throw new AppointmentAlreadyCancelledException(appointmentId);
//        }
//        appointment.setStatus(AppointmentStatus.CANCELLED);
//        appointmentRepository.save(appointment);
//
//        // --- SEND EMAILS ---
//        String customerEmail = appointment.getCustomer().getEmail();
//        String customerName = appointment.getCustomer().getName();
//        String barberEmail = appointment.getBarber().getEmail();
//        String barberName = appointment.getBarber().getName();
//        String serviceNames = appointment.getServices().stream()
//                .map(ServiceOffering::getName)
//                .collect(Collectors.joining(", "));
//        String date = appointment.getScheduledTime().toLocalDate().toString();
//
//        // Notify Customer
//        emailService.sendAppointmentCancellation(customerEmail, customerName, serviceNames, date, cancelledByName);
//
//        // Notify Barber
//        emailService.sendAppointmentCancellation(barberEmail, barberName, serviceNames, date, cancelledByName);
//    }
//
//    public List<LocalDateTime> computeAvailableSlots(
//            Barber barber,
//            LocalDate date,
//            List<ServiceOffering> services,
//            Long excludeAppointmentId
//    ) {
//
//        // --- NEW LEAVE CHECK ---
//        // If barber has approved leave overlapping this date, return empty list.
//        // Logic: StartDate <= date AND EndDate >= date
//        boolean isOnLeave = barberLeaveRepository
//                .existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
//                        barber,
//                        LeaveStatus.APPROVED,
//                        date,
//                        date
//                );
//
//        if (isOnLeave) {
//            return new ArrayList<>(); // No slots available
//        }
//        // -----------------------
//
//        int totalDurationMinutes = services.stream()
//                .mapToInt(ServiceOffering::getDurationMinutes)
//                .sum();
//
//        int slotIncrement = 30;
//
//        LocalDateTime startOfDay = date.atStartOfDay();
//        LocalDateTime endOfDay   = date.atTime(LocalTime.MAX);
//
//        List<Appointment> bookedAppointments =
//                appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
//                        barber,
//                        AppointmentStatus.SCHEDULED,
//                        startOfDay,
//                        endOfDay
//                );
//
//        LocalDateTime workStart = date.atTime(9, 0);
//        LocalDateTime workEnd   = date.atTime(18, 0);
//
//        List<LocalDateTime> availableSlots = new ArrayList<>();
//        LocalDateTime slotStart = workStart;
//
//        while (!slotStart.plusMinutes(totalDurationMinutes).isAfter(workEnd)) {
//
//            LocalDateTime slotEnd = slotStart.plusMinutes(totalDurationMinutes);
//
//            LocalDateTime finalSlotStart = slotStart;
//            boolean conflict = bookedAppointments.stream()
//                    .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
//                    .anyMatch(existing -> {
//
//                        LocalDateTime existingStart = existing.getScheduledTime();
//                        LocalDateTime existingEnd = existingStart.plusMinutes(existing.getTotalDurationMinutes());
//
//                        return finalSlotStart.isBefore(existingEnd)
//                                && slotEnd.isAfter(existingStart);
//                    });
//
//            if (!conflict) {
//                availableSlots.add(slotStart);
//            }
//
//            slotStart = slotStart.plusMinutes(slotIncrement);
//        }
//
//        return availableSlots;
//    }
//
//    public List<Appointment> getBookedAppointments(Barber barber, LocalDate startDate, LocalDate endDate) {
//        LocalDateTime startOfDay;
//        LocalDateTime endOfDay;
//        if (startDate == null && endDate == null) {
//            startOfDay = LocalDate.now().atStartOfDay();
//            endOfDay = LocalDate.now().atTime(23, 59, 59);
//        }
//        else if (startDate != null && endDate == null) {
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = startDate.atTime(23, 59, 59);
//        }
//        else {
//            assert startDate != null;
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = endDate.atTime(23, 59, 59);
//        }
//        return appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
//                barber,
//                AppointmentStatus.SCHEDULED,
//                startOfDay,
//                endOfDay
//        );
//    }
//
//    public Page<Appointment> getBarberAppointments(Barber barber, LocalDate startDate, LocalDate endDate, Pageable pageable) {
//        LocalDateTime startOfDay;
//        LocalDateTime endOfDay;
//
//        if (startDate == null && endDate == null) {
//            startOfDay = LocalDate.now().atStartOfDay();
//            endOfDay = LocalDate.now().atTime(23, 59, 59);
//        }
//        else if (startDate != null && endDate == null) {
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = startDate.atTime(23, 59, 59);
//        }
//        else {
//            assert startDate != null;
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = endDate.atTime(23, 59, 59);
//        }
//
//        return appointmentRepository.findByBarberAndScheduledTimeBetween(
//                barber,
//                startOfDay,
//                endOfDay,
//                pageable
//        );
//    }
//
//    public Page<Appointment> getAppointmentsForAdmin(Admin admin, Pageable pageable) {
//        if (admin.getRole() == Roles.MAIN_ADMIN) {
//            return appointmentRepository.findAll(pageable);
//
//        } else if (admin.getRole() == Roles.SHOP_ADMIN) {
//            return appointmentRepository.findAllByBarbershop(admin.getBarbershop(), pageable);
//
//        } else {
//            throw new AccessDeniedException("Invalid role for this action");
//        }
//    }
//
//    public AvailableSlotsResponseDTO getAvailability(Barber barber, List<ServiceOffering> services, LocalDate date) {
//        List<LocalDateTime> availableSlotTimes = computeAvailableSlots(barber, date, services,null);
//
//        List<Appointment> bookedAppointments = getBookedAppointments(barber, date, null);
//
//        List<TimeSlotDTO> availableSlots = appointmentSlotMapper.toAvailableSlots(
//                availableSlotTimes,
//                services.stream().mapToInt(ServiceOffering::getDurationMinutes).sum()
//        );
//
//        List<TimeSlotDTO> bookedSlots = appointmentSlotMapper.toTimeSlotDTOList(bookedAppointments);
//
//        return  appointmentSlotMapper.toAvailableSlotsResponse(
//                barber,
//                services,
//                date,
//                availableSlots,
//                bookedSlots
//        );
//    }
//
//    @Transactional
//    public Appointment reschedule(Appointment appointment, LocalDateTime newDateTime) {
//
//        if (newDateTime == null) {
//            throw new IllegalArgumentException("New date time cannot be null");
//        }
//
//        if (newDateTime.isBefore(LocalDateTime.now())) {
//            throw new IllegalArgumentException("Cannot reschedule to past time");
//        }
//
//        if (appointment.isCompleted()) {
//            throw new IllegalStateException("Completed appointment cannot be rescheduled");
//        }
//
//        if (appointment.isCancelled()) {
//            throw new IllegalStateException("Cancelled appointment cannot be rescheduled");
//        }
//
//        if (appointment.getScheduledTime().equals(newDateTime)) {
//            return appointment; // no change
//        }
//
//        validateSlotAvailability(appointment, newDateTime);
//
//        String oldDate = appointment.getScheduledTime().toLocalDate().toString();
//
//        appointment.setScheduledTime(newDateTime);
//        appointment.setCheckInTime(newDateTime.minusMinutes(10));
//        appointment.setCompletedTime(newDateTime.plusMinutes(appointment.getTotalDurationMinutes()));
//
//        Appointment saved = appointmentRepository.save(appointment);
//
//        // --- SEND EMAILS ---
//        String customerEmail = saved.getCustomer().getEmail();
//        String customerName = saved.getCustomer().getName();
//        String barberEmail = saved.getBarber().getEmail();
//        String barberName = saved.getBarber().getName();
//        String newDateStr = newDateTime.toLocalDate().toString();
//        String newTimeStr = newDateTime.toLocalTime().toString();
//        String serviceNames = saved.getServices().stream()
//                .map(ServiceOffering::getName)
//                .collect(Collectors.joining(", "));
//
//        // Notify Customer
//        emailService.sendAppointmentReschedule(customerEmail, customerName, serviceNames, oldDate, newDateStr, newTimeStr);
//
//        // Notify Barber
//        emailService.sendAppointmentReschedule(barberEmail, barberName, serviceNames, oldDate, newDateStr, newTimeStr);
//
//        return saved;
//    }
//
//    private void validateSlotAvailability(Appointment appointment, LocalDateTime newDateTime) {
//
//        if (newDateTime.getMinute() % 30 != 0) {
//            throw new InvalidAppointmentTimeException("Appointments must start at 30-min intervals");
//        }
//
//        List<LocalDateTime> availableSlots =
//                computeAvailableSlots(
//                        appointment.getBarber(),
//                        newDateTime.toLocalDate(),
//                        appointment.getServices(),
//                        appointment.getId()
//                );
//
//        if (!availableSlots.contains(newDateTime)) {
//            throw new AppointmentSlotUnavailableException("Selected slot is not available");
//        }
//    }
//
//    public Double getEarnings(Barber barber, LocalDate startDate, LocalDate endDate) {
//        LocalDate today = LocalDate.now();
//        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);
//        LocalDate friday = today.with(java.time.DayOfWeek.FRIDAY);
//
//        LocalDateTime startOfDay;
//        LocalDateTime endOfDay;
//
//        if (startDate == null && endDate == null) {
//            startOfDay = sunday.atStartOfDay();
//            endOfDay = friday.atTime(23, 59, 59);
//        }
//        else if (startDate != null && endDate == null) {
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = friday.atTime(23, 59, 59);
//        }
//        else {
//            startOfDay = startDate.atStartOfDay();
//            endOfDay = endDate.atTime(23, 59, 59);
//        }
//
//        Double earnings = appointmentRepository
//                .sumEarningsByBarberAndDate(barber, startOfDay, endOfDay);
//
//        return earnings != null ? earnings : 0.0;
//    }
//
//    public long count() {
//        return appointmentRepository.count();
//    }
//
//    public Integer countByShopAndScheduledTimeBetween(Barbershop shop, LocalDateTime startOfDay, LocalDateTime endOfDay) {
//        return appointmentRepository.countByBarbershopAndScheduledTimeBetween(shop,startOfDay,endOfDay);
//    }
//
//    public Double sumRevenueByShopAndScheduledTimeBetween(Barbershop shop, LocalDateTime startOfDay, LocalDateTime endOfDay) {
//        return appointmentRepository.sumRevenueByBarbershopAndScheduledTimeBetween(shop,startOfDay,endOfDay);
//    }
//
//    public Integer countByShopAndStatus(Barbershop shop, AppointmentStatus appointmentStatus) {
//        return appointmentRepository.countByBarbershopAndStatus(shop, appointmentStatus);
//    }
//
//    public List<Appointment> findUpcomingByShop(Barbershop shop, LocalDateTime now, PageRequest of) {
//        return appointmentRepository.findUpcomingByBarbershop(shop, now, of);
//    }
//}
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
import com.sijan.barberReservation.repository.BarberLeaveRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sijan.barberReservation.repository.SlotReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final EmailService emailService;
    private final BarberLeaveRepository barberLeaveRepository;
    private final SlotReservationRepository slotReservationRepository;

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
                computeAvailableSlots(appointment.getBarber(), appointment.getScheduledTime().toLocalDate(), appointment.getServices(), null);

        if (!availableSlots.contains(appointment.getScheduledTime())) {
            throw new AppointmentSlotUnavailableException("Selected slot is not available");
        }

        LocalDateTime checkInTime = appointment.getScheduledTime().minusMinutes(10);
        appointment.setCheckInTime(checkInTime);
        appointment.setCustomer(customer);
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        // ✅ Safety net: DB Unique Constraint will catch race conditions if called from PaymentService
        try {
            Appointment savedAppointment = appointmentRepository.save(appointment);
            sendBookingEmails(savedAppointment, customer);
            return savedAppointment;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_barber_scheduled_time")) {
                throw new AppointmentSlotUnavailableException(
                        "This slot was just booked by another customer. Please select a different time."
                );
            }
            throw e;
        }
    }

    private void sendBookingEmails(Appointment savedAppointment, Customer customer) {
        String customerEmail = customer.getEmail();
        String customerName = customer.getName();
        String barberEmail = savedAppointment.getBarber().getEmail();
        String barberName = savedAppointment.getBarber().getName();
        String shopName = savedAppointment.getBarber().getBarbershop().getName();
        String serviceNames = savedAppointment.getServices().stream()
                .map(ServiceOffering::getName)
                .collect(Collectors.joining(", "));
        String date = savedAppointment.getScheduledTime().toLocalDate().toString();
        String time = savedAppointment.getScheduledTime().toLocalTime().toString();

        emailService.sendAppointmentConfirmationCustomer(customerEmail, customerName, barberName, serviceNames, date, time, shopName);
        emailService.sendNewBookingAlert(barberEmail, barberName, customerName, serviceNames, date, time);
    }

    public Page<Appointment> getUpcomingByCustomer(Customer customer, int page, int size) {
        return appointmentRepository.findUpcomingByCustomer(customer, LocalDateTime.now(), PageRequest.of(page, size));
    }

    public Page<Appointment> getPastByCustomer(Customer customer, int page, int size) {
        return appointmentRepository.findPastByCustomer(customer, LocalDateTime.now(), PageRequest.of(page, size));
    }

    public Page<Appointment> getUpcomingByBarber(Barber barber, int page, int size) {
        return appointmentRepository.findUpcomingByBarber(barber, LocalDateTime.now(), PageRequest.of(page, size));
    }

    public Page<Appointment> getPastByBarber(Barber barber, int page, int size) {
        return appointmentRepository.findPastByBarber(barber, LocalDateTime.now(), PageRequest.of(page, size));
    }

    @Transactional
    public void cancel(Long appointmentId, String cancelledByName) {
        Appointment appointment = findById(appointmentId);
        if (appointment.getStatus().equals(AppointmentStatus.CANCELLED)) {
            throw new AppointmentAlreadyCancelledException(appointmentId);
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        String customerEmail = appointment.getCustomer().getEmail();
        String customerName = appointment.getCustomer().getName();
        String barberEmail = appointment.getBarber().getEmail();
        String barberName = appointment.getBarber().getName();
        String serviceNames = appointment.getServices().stream().map(ServiceOffering::getName).collect(Collectors.joining(", "));
        String date = appointment.getScheduledTime().toLocalDate().toString();

        emailService.sendAppointmentCancellation(customerEmail, customerName, serviceNames, date, cancelledByName);
        emailService.sendAppointmentCancellation(barberEmail, barberName, serviceNames, date, cancelledByName);
    }

    /**
     * ✅ PURE LOGIC: Computes slots based ONLY on actual booked appointments.
     * Does NOT look at temporary payment locks (that is PaymentService's job).
     */
    public List<LocalDateTime> computeAvailableSlots(
            Barber barber,
            LocalDate date,
            List<ServiceOffering> services,
            Long excludeAppointmentId
    ) {
        boolean isOnLeave = barberLeaveRepository
                .existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(barber, LeaveStatus.APPROVED, date, date);

        if (isOnLeave) {
            return new ArrayList<>();
        }

        int totalDurationMinutes = services.stream().mapToInt(ServiceOffering::getDurationMinutes).sum();
        int slotIncrement = 30;

        LocalDateTime workStart = date.atTime(9, 0);
        LocalDateTime workEnd = date.atTime(18, 0);

        // 1. Get actual booked appointments
        List<Appointment> bookedAppointments =
                appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
                        barber,
                        AppointmentStatus.SCHEDULED,
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)
                );

        // ✅ 2. GET ACTIVE RESERVATIONS (Pending Payments)
        List<LocalDateTime> reservedSlotTimes = slotReservationRepository
                .findActiveByBarberAndDate(
                        barber.getId(),
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX)
                )
                .stream()
                .map(SlotReservation::getReservedTime)
                .collect(Collectors.toList());

        List<LocalDateTime> availableSlots = new ArrayList<>();
        LocalDateTime slotStart = workStart;

        while (!slotStart.plusMinutes(totalDurationMinutes).isAfter(workEnd)) {
            LocalDateTime slotEnd = slotStart.plusMinutes(totalDurationMinutes);
            LocalDateTime finalSlotStart = slotStart;

            // ✅ 3. CHECK IF SLOT IS TEMPORARILY RESERVED BY SOMEONE PAYING
            boolean isReserved = reservedSlotTimes.contains(finalSlotStart);

            boolean conflict = bookedAppointments.stream()
                    .filter(a -> excludeAppointmentId == null || !a.getId().equals(excludeAppointmentId))
                    .anyMatch(existing -> {
                        LocalDateTime existingStart = existing.getScheduledTime();
                        LocalDateTime existingEnd = existingStart.plusMinutes(existing.getTotalDurationMinutes());
                        return finalSlotStart.isBefore(existingEnd) && slotEnd.isAfter(existingStart);
                    });

            // ✅ 4. ONLY ADD IF NOT RESERVED AND NOT CONFLICTING
            if (!isReserved && !conflict) {
                availableSlots.add(slotStart);
            }

            slotStart = slotStart.plusMinutes(slotIncrement);
        }
        return availableSlots;
    }

    public List<Appointment> getBookedAppointments(Barber barber, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfDay;
        LocalDateTime endOfDay;
        if (startDate == null && endDate == null) {
            startOfDay = LocalDate.now().atStartOfDay();
            endOfDay = LocalDate.now().atTime(23, 59, 59);
        } else if (startDate != null && endDate == null) {
            startOfDay = startDate.atStartOfDay();
            endOfDay = startDate.atTime(23, 59, 59);
        } else {
            startOfDay = startDate.atStartOfDay();
            endOfDay = endDate.atTime(23, 59, 59);
        }
        return appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(barber, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);
    }

    public Page<Appointment> getBarberAppointments(Barber barber, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        LocalDateTime startOfDay;
        LocalDateTime endOfDay;
        if (startDate == null && endDate == null) {
            startOfDay = LocalDate.now().atStartOfDay();
            endOfDay = LocalDate.now().atTime(23, 59, 59);
        } else if (startDate != null && endDate == null) {
            startOfDay = startDate.atStartOfDay();
            endOfDay = startDate.atTime(23, 59, 59);
        } else {
            startOfDay = startDate.atStartOfDay();
            endOfDay = endDate.atTime(23, 59, 59);
        }
        return appointmentRepository.findByBarberAndScheduledTimeBetween(barber, startOfDay, endOfDay, pageable);
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

    // ✅ Maps correctly to your AvailableSlotsResponseDTO
    public AvailableSlotsResponseDTO getAvailability(Barber barber, List<ServiceOffering> services, LocalDate date) {
        List<LocalDateTime> availableSlotTimes = computeAvailableSlots(barber, date, services, null);
        List<Appointment> bookedAppointments = getBookedAppointments(barber, date, null);

        List<TimeSlotDTO> availableSlots = appointmentSlotMapper.toAvailableSlots(
                availableSlotTimes,
                services.stream().mapToInt(ServiceOffering::getDurationMinutes).sum()
        );

        List<TimeSlotDTO> bookedSlots = appointmentSlotMapper.toTimeSlotDTOList(bookedAppointments);

        return appointmentSlotMapper.toAvailableSlotsResponse(barber, services, date, availableSlots, bookedSlots);
    }

    @Transactional
    public Appointment reschedule(Appointment appointment, LocalDateTime newDateTime) {
        if (newDateTime == null) throw new IllegalArgumentException("New date time cannot be null");
        if (newDateTime.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("Cannot reschedule to past time");
        if (appointment.isCompleted()) throw new IllegalStateException("Completed appointment cannot be rescheduled");
        if (appointment.isCancelled()) throw new IllegalStateException("Cancelled appointment cannot be rescheduled");
        if (appointment.getScheduledTime().equals(newDateTime)) return appointment;

        validateSlotAvailability(appointment, newDateTime);
        String oldDate = appointment.getScheduledTime().toLocalDate().toString();

        appointment.setScheduledTime(newDateTime);
        appointment.setCheckInTime(newDateTime.minusMinutes(10));
        appointment.setCompletedTime(newDateTime.plusMinutes(appointment.getTotalDurationMinutes()));

        // ✅ Safety net: DB Unique Constraint catch
        try {
            Appointment saved = appointmentRepository.save(appointment);
            String customerEmail = saved.getCustomer().getEmail();
            String customerName = saved.getCustomer().getName();
            String barberEmail = saved.getBarber().getEmail();
            String barberName = saved.getBarber().getName();
            String newDateStr = newDateTime.toLocalDate().toString();
            String newTimeStr = newDateTime.toLocalTime().toString();
            String serviceNames = saved.getServices().stream().map(ServiceOffering::getName).collect(Collectors.joining(", "));

            emailService.sendAppointmentReschedule(customerEmail, customerName, serviceNames, oldDate, newDateStr, newTimeStr);
            emailService.sendAppointmentReschedule(barberEmail, barberName, serviceNames, oldDate, newDateStr, newTimeStr);
            return saved;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_barber_scheduled_time")) {
                throw new AppointmentSlotUnavailableException("This slot was just booked. Please select a different time.");
            }
            throw e;
        }
    }

    private void validateSlotAvailability(Appointment appointment, LocalDateTime newDateTime) {
        if (newDateTime.getMinute() % 30 != 0) {
            throw new InvalidAppointmentTimeException("Appointments must start at 30-min intervals");
        }
        List<LocalDateTime> availableSlots = computeAvailableSlots(appointment.getBarber(), newDateTime.toLocalDate(), appointment.getServices(), appointment.getId());
        if (!availableSlots.contains(newDateTime)) {
            throw new AppointmentSlotUnavailableException("Selected slot is not available");
        }
    }

    public Double getEarnings(Barber barber, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);
        LocalDate friday = today.with(java.time.DayOfWeek.FRIDAY);

        LocalDateTime startOfDay;
        LocalDateTime endOfDay;
        if (startDate == null && endDate == null) {
            startOfDay = sunday.atStartOfDay();
            endOfDay = friday.atTime(23, 59, 59);
        } else if (startDate != null && endDate == null) {
            startOfDay = startDate.atStartOfDay();
            endOfDay = friday.atTime(23, 59, 59);
        } else {
            startOfDay = startDate.atStartOfDay();
            endOfDay = endDate.atTime(23, 59, 59);
        }
        Double earnings = appointmentRepository.sumEarningsByBarberAndDate(barber, startOfDay, endOfDay);
        return earnings != null ? earnings : 0.0;
    }

    public long count() { return appointmentRepository.count(); }
    public Integer countByShopAndScheduledTimeBetween(Barbershop shop, LocalDateTime startOfDay, LocalDateTime endOfDay) { return appointmentRepository.countByBarbershopAndScheduledTimeBetween(shop, startOfDay, endOfDay); }
    public Double sumRevenueByShopAndScheduledTimeBetween(Barbershop shop, LocalDateTime startOfDay, LocalDateTime endOfDay) { return appointmentRepository.sumRevenueByBarbershopAndScheduledTimeBetween(shop, startOfDay, endOfDay); }
    public Integer countByShopAndStatus(Barbershop shop, AppointmentStatus appointmentStatus) { return appointmentRepository.countByBarbershopAndStatus(shop, appointmentStatus); }
    public List<Appointment> findUpcomingByShop(Barbershop shop, LocalDateTime now, PageRequest of) { return appointmentRepository.findUpcomingByBarbershop(shop, now, of); }
}
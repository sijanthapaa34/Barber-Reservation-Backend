package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberRepository;
import com.sijan.barberReservation.repository.AppointmentRepository;
import com.sijan.barberReservation.repository.CustomerRepository;
import com.sijan.barberReservation.repository.ServiceRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            BarberRepository barberRepository,
            ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
    }

    public DetailsDTO bookNewAppointment(CreateAppointmentRequest request, String email) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        System.out.println(request.getBarberId());
        Barber barber =  barberRepository.findById(request.getBarberId())
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Services service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        LocalDateTime appointmentDateTime = request.getAppointmentDateTime();
        System.out.println(appointmentDateTime + "yo ma error aako");
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book an appointment in the past");
        }

        LocalDateTime checkInTime = appointmentDateTime.minusMinutes(10);

        Appointment newAppointment = new Appointment();
        newAppointment.setBarber(barber);
        newAppointment.setCustomer(customer);
        newAppointment.setService(service);
        newAppointment.setStatus(AppointmentStatus.PENDING);
        newAppointment.setCheckInTime(checkInTime);
        newAppointment.setScheduledTime(appointmentDateTime);
        newAppointment.setCreatedAt(LocalDateTime.now());

        appointmentRepository.save(newAppointment);

        return toDetailsDTO(newAppointment);
    }


    public DetailsDTO viewAppointmentDetails(Long appointmentId, String email) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

//        if (!appointment.getCustomer().getId().equals(userId) && !appointment.getBarber().getId().equals(userId)) {
//            throw new RuntimeException("Access denied");
//        }
        return toDetailsDTO(appointment);
    }

    public String cancelAppointment(Long id, String email, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        if (!appointment.getCustomer().getEmail().equals(email) || !appointment.getBarber().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.save(appointment);
        return "Cancelled Successfully";
    }

    public DetailsDTO rescheduleAppointment(Long id, String email, RescheduleAppointmentRequest request) {
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
        log.setAction(AppointmentStatus.RESCHEDULED);
        log.setDescription(request.getReason());
        log.setPerformedBy(oldAppointment.getCustomer().getName());

        appointmentRepository.save(oldAppointment);
        return toDetailsDTO(oldAppointment);

    }

    private DetailsDTO toDetailsDTO(Appointment appointment){
        DetailsDTO dto= new DetailsDTO();
        dto.setCustomerName(appointment.getCustomer().getName());
        dto.setBarberName(appointment.getBarber().getName());
        dto.setServiceName(appointment.getService().getName());
        dto.setServicePrice(appointment.getService().getPrice());
        dto.setScheduledTime(appointment.getScheduledTime());
        dto.setCheckInTime(appointment.getCheckInTime());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setStatus(appointment.getStatus().name());

        return dto;
    }

    public List<DetailsDTO> getUserAppointments(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointments = appointmentRepository.findByCustomerEmail(email, pageable);

        return appointments.getContent().stream()
                .map(this::toDetailsDTO)
                .collect(Collectors.toList());
    }


    public AvailableSlotsResponseDTO getAvailableSlots(Long barberId, LocalDate date, Long serviceId) {
        // 1. Fetch required data
        System.out.println(barberId);
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new RuntimeException("Barber not found"));

        Services service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        List<Appointment> bookedAppointments = appointmentRepository.findByBarberIdAndScheduledDate(barberId, date);

        int durationMinutes = service.getDurationMinutes();
        Duration serviceDuration = Duration.ofMinutes(durationMinutes);

        // 2. Define working hours
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        // 3. Booked slot map
        Set<LocalTime> bookedStartTimes = bookedAppointments.stream()
                .map(Appointment::getScheduledTime)
                .map(LocalDateTime::toLocalTime)
                .collect(Collectors.toSet());

        // 4. Generate all time slots
        List<TimeSlotDTO> allSlots = new ArrayList<>();
        for (LocalTime slot = startTime; slot.plus(serviceDuration).isBefore(endTime.plusSeconds(1));
             slot = slot.plus(serviceDuration)) {

            boolean isBooked = bookedStartTimes.contains(slot);

            TimeSlotDTO slotDTO = TimeSlotDTO.builder()
                    .startTime(slot)
                    .endTime(slot.plus(serviceDuration))
                    .date(date)
                    .dateTime(LocalDateTime.of(date, slot))
                    .available(!isBooked)
                    .status(isBooked ? AppointmentStatus.BOOKED : AppointmentStatus.AVAILABLE)
                    .duration(serviceDuration)
                    .displayTime(formatDisplayTime(slot, slot.plus(serviceDuration)))
                    .displayDate(formatDisplayDate(date))
                    .isRecommended(false)  // we'll set one later
                    .unavailableReason(isBooked ? "Already booked" : null)
                    .build();

            allSlots.add(slotDTO);
        }

        // 5. Mark first available as recommended
        allSlots.stream()
                .filter(TimeSlotDTO::isAvailable)
                .findFirst()
                .ifPresent(slot -> slot.setRecommended(true));

        List<TimeSlotDTO> availableSlots = allSlots.stream()
                .filter(TimeSlotDTO::isAvailable)
                .collect(Collectors.toList());

        List<TimeSlotDTO> bookedSlots = allSlots.stream()
                .filter(slot -> !slot.isAvailable())
                .collect(Collectors.toList());

        // 6. Build response
        return AvailableSlotsResponseDTO.builder()
                .date(date)
                .barberName(barber.getName())
                .serviceName(service.getName())
                .serviceDuration(serviceDuration)
                .availableSlots(availableSlots)
                .bookedSlots(bookedSlots)
                .totalAvailableSlots(availableSlots.size())
                .nextAvailableSlot(availableSlots.isEmpty() ? null : availableSlots.get(0).getStartTime().toString())
                .hasAvailableSlots(!availableSlots.isEmpty())
                .build();
    }

    private String formatDisplayTime(LocalTime start, LocalTime end) {
        return String.format("%s - %s", start.format(DateTimeFormatter.ofPattern("hh:mm a")),
                end.format(DateTimeFormatter.ofPattern("hh:mm a")));
    }

    private String formatDisplayDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")); // e.g., "December 25, 2025"
    }

    public List<DetailsDTO> getBarberAppointments(String barberEmail, LocalDate targetDate) {
        List<Appointment> appointments = appointmentRepository.findByBarberEmailAndScheduledDate(barberEmail, targetDate);

        return appointments.stream()
                .map(this::toDetailsDTO)
                .collect(Collectors.toList());
    }

    public List<DetailsDTO> getAllAppointments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledTime").descending());
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);

        List<DetailsDTO> detailsDTOS = new ArrayList<>();
        for(Appointment appointment: appointments){
            detailsDTOS.add(toDetailsDTO(appointment));
        }

        return detailsDTOS;
    }
}

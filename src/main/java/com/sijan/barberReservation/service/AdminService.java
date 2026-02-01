package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.DTO.service.ServiceRequest;
import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final BarberRepository barberRepository;
    private final BarberLeaveRepository barberLeaveRepository;
    private final ServiceRepository serviceRepository;
    private final AdminRepository adminRepository;
    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;

    public AdminService(
            BarberRepository barberRepository,
            BarberLeaveRepository barberLeaveRepository,
            ServiceRepository serviceRepository, AdminRepository adminRepository,
            AppointmentRepository appointmentRepository, CustomerRepository customerRepository) {
        this.barberRepository = barberRepository;
        this.barberLeaveRepository = barberLeaveRepository;
        this.serviceRepository = serviceRepository;
        this.adminRepository = adminRepository;
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
    }
    public Admin findById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(()-> new RuntimeException("Admin id not found"));
    }


    // In AdminService.java
//    public BarberDTO addBarber(Long adminId, CreateBarberRequest request) {
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("A user with this email already exists");
//        }
//
//        User user = new User();
//        user.setName(request.getName());
//        user.setEmail(request.getEmail());
//        user.setPhone(request.getPhone());
//        user.setRole(Role.BARBER);
//        user.setPassword(passwordEncoder.encode(request.getPassword())); // Always encode
//        user.setCreatedAt(LocalDateTime.now());
//        user = userRepository.save(user);
//
//        Barber barber = new Barber();
//        barber.setUser(user);
//        barber.setBio(request.getBio());
//        barber.setActive(true); // Active by default
//        barber.setAverageRating(0.0);
//        barber.setTotalRatings(0);
//        barber.setCreatedAt(LocalDateTime.now());
//
//        barber = barberRepository.save(barber);
//
//        BarberDTO dto = new BarberDTO();
//        dto.setId(barber.getId());
//        dto.setName(barber.getName());
//        dto.setEmail(barber.getEmail());
//        dto.setPhone(barber.getPhone());
//        dto.setBio(barber.getBio());
//        dto.setActive(barber.isActive());
//        dto.setAverageRating(barber.getAverageRating());
//        dto.setTotalRatings(barber.getTotalRatings());
//        dto.setCreatedAt(barber.getCreatedAt());
//
//        return dto;
//    }

    public List<BarberDTO> getAllBarbers() {
        List<Barber> barbers = barberRepository.findAll();
        List<BarberDTO> barberDTO = new ArrayList<>();
        for (Barber barber: barbers){
            BarberDTO dto = new BarberDTO();
            dto.setName(barber.getName());
            dto.setEmail(barber.getEmail());
            dto.setPhone(barber.getPhone());
            dto.setActive(barber.getActive());
            dto.setBio(barber.getBio());
            dto.setProfilePictureUrl(barber.getProfilePicture());
            dto.setRating(barber.getRating());
            dto.setCreatedAt(barber.getCreatedAt());
            barberDTO.add(dto);
            }
            return barberDTO;
    }
    public void activateBarber( Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        barber.setActive(true);
        barberRepository.save(barber);
    }

    public void deactivateBarber( Long id) {
        Barber barber = barberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barber not found"));
        barber.setActive(false);
        barberRepository.save(barber);
    }

    public List<ServiceDTO> getAllServices(Long adminId) {

        List<ServiceOffering> Service = serviceRepository.findAll();
        List<ServiceDTO> serviceDTO = new ArrayList<>();
        for(ServiceOffering service: Service){
            ServiceDTO dto = new ServiceDTO();
            dto.setName(service.getName());
            dto.setDurationMinutes(service.getDurationMinutes());
            dto.setPrice(service.getPrice());
            serviceDTO.add(dto);
        }

        return serviceDTO;
    }

    public ServiceDTO addService(Long adminId, ServiceRequest request) {
        ServiceOffering service = new ServiceOffering();
        service.setName(request.getName());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());
        service.setAvailable(true);
        ServiceOffering savedService = serviceRepository.save(service);

        ServiceDTO dto = new ServiceDTO();
        dto.setName(savedService.getName());
        dto.setDurationMinutes(savedService.getDurationMinutes());
        dto.setPrice(savedService.getPrice());
        return dto;
    }

    public ServiceDTO updateService(Long adminId, Long id, ServiceRequest request) {
        ServiceOffering service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (request.getName() != null) service.setName(request.getName());
        if (request.getDurationMinutes() != 0) service.setDurationMinutes(request.getDurationMinutes());
        if (request.getPrice() != null) service.setPrice(request.getPrice());

        service = serviceRepository.save(service);

        ServiceDTO dto = new ServiceDTO();
        dto.setName(service.getName());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setPrice(service.getPrice());
        return dto;
    }

    public void activateService(Long adminId, Long id) {
        ServiceOffering service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setAvailable(true);
        serviceRepository.save(service);
    }

    public void deactivateService(Long adminId, Long id) {
        ServiceOffering service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setAvailable(false);
        serviceRepository.save(service);
    }


    public List<FrequentCustomerDTO> getFrequentCustomers(Long adminId) {
        List<Customer> results = customerRepository.findTop5ByRoleOrderByTotalBookingsDesc(Roles.valueOf("CUSTOMER"));
        List<FrequentCustomerDTO> frequentCustomerDTO = new ArrayList<>();
        for(Customer customer : results) {
            FrequentCustomerDTO dto = new FrequentCustomerDTO();
            dto.setName(customer.getName());
            dto.setEmail(customer.getEmail());
            dto.setTotalBookings(customer.getTotalBookings());
            dto.setPoints(customer.getPoints());

            frequentCustomerDTO.add(dto);
        }
        return frequentCustomerDTO;
    }

    public List<BarberLeaveDTO> getAllBarberLeaves(Long adminId, LeaveStatus status) {
        List<BarberLeave> leaves = (status == null)
                ? barberLeaveRepository.findAll()
                : barberLeaveRepository.findByStatus(status);

        List<BarberLeaveDTO> barberDTOs = new ArrayList<>();
        for(BarberLeave leave: leaves){
            BarberLeaveDTO dto = new BarberLeaveDTO();
            dto.setBarberName(leave.getBarber().getName());
            dto.setStartDate(leave.getStartDate());
            dto.setEndDate(leave.getEndDate());
            dto.setReason(leave.getReason());
            dto.setStatus(leave.getStatus());
            dto.setRequestedAt(leave.getRequestedAt());
            dto.setApprovedAt(leave.getApprovedAt());
            dto.setRejectedAt(leave.getRejectedAt());
            barberDTOs.add(dto);

        }
        return barberDTOs;
    }

    public void updateLeaveStatus(Long leaveId, Long barberId, LeaveStatus leaveStatus, Long adminId) {

        BarberLeave leave = barberLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        if (!leave.getBarber().getId().equals(barberId)) {
            throw new RuntimeException("Leave does not belong to this barber");
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be updated");
        }
        leave.setStatus(leaveStatus);
//        if (leaveStatus == LeaveStatus.APPROVED) {
//            leave.setApprovedAt(LocalDateTime.now());
//        } else if (newStatus == LeaveStatus.REJECTED) {
//            leave.setRejectedAt(LocalDateTime.now());
//        }
        barberLeaveRepository.save(leave);
        //logLeaveAction(leave, newStatus, adminId);
        //notificationService.sendLeaveStatusUpdate(leave, newStatus);
    }


    public void register(Admin admin) {
        adminRepository.save(admin);
    }
}

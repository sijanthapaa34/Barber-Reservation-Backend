package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.user.*;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Barber> getAllBarberLeaves(Pageable page, Long adminId, LeaveStatus status) {
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

    public Admin findByEmail(String adminEmail) {
        return adminRepository.findByEmail(adminEmail);
    }
}

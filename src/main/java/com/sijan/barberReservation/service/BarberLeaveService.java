package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BarberLeaveService {

    private final BarberLeaveRepository barberLeaveRepository;
    private final EmailService emailService; // INJECTED

    // Helper to format dates nicely for email
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public BarberLeave findById(Long leaveId) {
        return barberLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));
    }

    @Transactional
    public void applyForLeave(Barber barber, BarberLeave leave) {
        leave.setBarber(barber);
        leave.setBarbershop(barber.getBarbershop());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setRequestedAt(LocalDateTime.now());

        BarberLeave savedLeave = barberLeaveRepository.save(leave);

        // --- SEND EMAIL TO ADMIN ---
        // Assuming Barbershop has an 'admin' relationship (OneToOne)
        // If not, you need to inject AdminRepository and findByBarbershop(barber.getBarbershop())
        Admin admin = barber.getBarbershop().getAdmin();

        if (admin != null) {
            emailService.sendLeaveRequestNotificationAdmin(
                    admin.getEmail(),
                    barber.getName(),
                    savedLeave.getStartDate().format(dateFormatter),
                    savedLeave.getEndDate().format(dateFormatter),
                    savedLeave.getReason()
            );
        }
    }

    @Transactional
    public BarberLeave approveLeave(BarberLeave leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already processed.");
        }
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setProcessedAt(LocalDateTime.now());

        BarberLeave savedLeave = barberLeaveRepository.save(leave);

        // --- SEND EMAIL TO BARBER ---
        emailService.sendLeaveApprovalNotification(
                savedLeave.getBarber().getEmail(),
                savedLeave.getBarber().getName(),
                savedLeave.getStartDate().format(dateFormatter),
                savedLeave.getEndDate().format(dateFormatter)
        );

        return savedLeave;
    }

    @Transactional
    public BarberLeave rejectLeave(BarberLeave leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already processed.");
        }
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setProcessedAt(LocalDateTime.now());

        BarberLeave savedLeave = barberLeaveRepository.save(leave);

        // --- SEND EMAIL TO BARBER ---
        emailService.sendLeaveRejectionNotification(
                savedLeave.getBarber().getEmail(),
                savedLeave.getBarber().getName(),
                savedLeave.getStartDate().format(dateFormatter),
                savedLeave.getEndDate().format(dateFormatter)
        );

        return savedLeave;
    }

    public Page<BarberLeave> getLeavesByShop(Barbershop barbershop, Pageable pageable) {
        return barberLeaveRepository.findByBarbershopOrderByRequestedAtDesc(barbershop, pageable);
    }

    public Page<BarberLeave> getLeavesByBarber(Barber barber, Pageable pageable) {
        return barberLeaveRepository.findByBarberOrderByRequestedAtDesc(barber, pageable);
    }

    public boolean isOnLeave(Barber barber, LeaveStatus leaveStatus, LocalDate date, LocalDate date1) {
        return barberLeaveRepository.existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(barber, leaveStatus, date, date);
    }
}
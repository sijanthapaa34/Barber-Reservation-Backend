package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BarberLeaveService {
    private final BarberLeaveRepository barberLeaveRepository;

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
        barberLeaveRepository.save(leave);
    }

    @Transactional
    public BarberLeave approveLeave(BarberLeave leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already processed.");
        }
        leave.setStatus(LeaveStatus.APPROVED);
        leave.setProcessedAt(LocalDateTime.now());

        return barberLeaveRepository.save(leave);
    }

    @Transactional
    public BarberLeave rejectLeave(BarberLeave leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave request is already processed.");
        }
        leave.setStatus(LeaveStatus.REJECTED);
        leave.setProcessedAt(LocalDateTime.now());
        return barberLeaveRepository.save(leave);
    }

    public Page<BarberLeave> getLeavesByShop(Barbershop barbershop, Pageable pageable) {
        return barberLeaveRepository.findByBarbershopOrderByRequestedAtDesc(barbershop,pageable);
    }

    public Page<BarberLeave> getLeavesByBarber(Barber barber, Pageable pageable) {
        return barberLeaveRepository.findByBarberOrderByRequestedAtDesc(barber, pageable);
    }
}

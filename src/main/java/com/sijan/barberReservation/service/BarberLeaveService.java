package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.LeaveStatus;
import com.sijan.barberReservation.repository.BarberLeaveRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BarberLeaveService {
    private final BarberLeaveRepository barberLeaveRepository;

    public BarberLeaveService(BarberLeaveRepository barberLeaveRepository) {
        this.barberLeaveRepository = barberLeaveRepository;

    }

    @Transactional
    public void updateLeaveStatus(
            BarberLeave leave,
            Barber barber,
            LeaveStatus newStatus,
            Admin admin
    ) {
        if (!leave.getBarber().equals(barber)) {
            throw new IllegalArgumentException("Leave does not belong to this barber");
        }

        if (!leave.getBarbershop().equals(admin.getBarbershop())) {
            throw new AccessDeniedException("Not authorized for this shop");
        }

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Leave already processed");
        }

        leave.setStatus(newStatus);

        if (newStatus == LeaveStatus.APPROVED) {
            leave.setApprovedAt(LocalDateTime.now());
        } else if (newStatus == LeaveStatus.REJECTED) {
            leave.setRejectedAt(LocalDateTime.now());
        }
    }

    public BarberLeave findById(Long leaveId) {
        return barberLeaveRepository.findById(leaveId)
                .orElseThrow(()-> new RuntimeException("Leave id not found"));
    }
}

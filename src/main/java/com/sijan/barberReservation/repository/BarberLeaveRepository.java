package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarberLeaveRepository extends JpaRepository<BarberLeave, Long> {
//    List<BarberLeave> findByEmail(String mail);
    List<BarberLeave> findByStatus(LeaveStatus status);
}

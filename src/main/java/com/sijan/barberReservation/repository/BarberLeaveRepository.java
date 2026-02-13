package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarberLeaveRepository extends JpaRepository<BarberLeave, Long> {
    Page<BarberLeave> findByBarbershopAndStatus(Barbershop barberShop, Pageable pageable, LeaveStatus leaveStatus);
}

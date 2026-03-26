package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberLeave;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BarberLeaveRepository extends JpaRepository<BarberLeave, Long> {
    Page<BarberLeave> findByBarbershopOrderByRequestedAtDesc(Barbershop barbershop,Pageable pageable);
    Page<BarberLeave> findByBarberOrderByRequestedAtDesc(Barber barber,Pageable pageable);
    Page<BarberLeave> findByBarbershopAndStatus(Barbershop barbershop,Pageable pageable,LeaveStatus status);

    boolean existsByBarberAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Barber barber,
            LeaveStatus status,
            LocalDate endDate,
            LocalDate startDate
    );
}

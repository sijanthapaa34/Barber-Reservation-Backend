package com.sijan.barberReservation.repository;


import com.sijan.barberReservation.DTO.appointment.DetailsDTO;
import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.AppointmentStatus;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Page<Appointment> findUpcomingByCustomer(Customer customer, LocalDate now, Pageable pageable);
    Page<Appointment> findPastByCustomer(Customer customer, LocalDate now, Pageable pageable);
    List<Appointment> findByBarberAndScheduledTimeBetweenAndStatusIn(Barber barber, LocalDateTime localDateTime, LocalDateTime localDateTime1, List<AppointmentStatus> pending);
    List<Appointment> findByBarberAndScheduledTime(Barber barber, LocalDate targetDate);
}

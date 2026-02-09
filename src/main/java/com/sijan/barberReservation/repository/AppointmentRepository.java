package com.sijan.barberReservation.repository;


import com.sijan.barberReservation.model.*;
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

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.customer = :customer
      AND a.scheduledTime >= :from
    ORDER BY a.scheduledTime ASC
""")
    Page<Appointment> findUpcomingByCustomer(@Param("customer") Customer customer, @Param("from") LocalDateTime from, Pageable pageable);

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.customer = :customer
      AND a.scheduledTime < :to
    ORDER BY a.scheduledTime DESC
""")
    Page<Appointment> findPastByCustomer(@Param("customer") Customer customer, @Param("to") LocalDateTime to, Pageable pageable);

    List<Appointment> findByBarberAndStatusAndScheduledTimeBetween(Barber barber, AppointmentStatus status, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByBarberAndScheduledTimeBetween(Barber barber, LocalDateTime dayStart, LocalDateTime dayEnd);

    Page<Appointment> findAllByBarbershop(BarberShop shop, Pageable pageable);
}

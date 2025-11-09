package com.sijan.barberReservation.repository;


import com.sijan.barberReservation.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBarberEmailAndScheduledDate( String barberEmail,
                                                     LocalDate date);

    Page<Appointment> findByCustomerEmail(String email, Pageable pageable);

    List<Appointment> findByBarberIdAndScheduledDate(Long barberId, LocalDate date);
}

package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Barbershop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    Optional<Barber> findByEmail(String email);

    Page<Barber> findByBarbershop(Barbershop barbershop, Pageable pageable);
}

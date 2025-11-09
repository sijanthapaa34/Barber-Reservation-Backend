package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    Optional<Barber> findByEmail(String email);

}

package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarbershopRepository extends JpaRepository<Barbershop, Long> {
}

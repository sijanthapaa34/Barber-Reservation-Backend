package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.BarberShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarberShopRepository extends JpaRepository<BarberShop, Long> {
}

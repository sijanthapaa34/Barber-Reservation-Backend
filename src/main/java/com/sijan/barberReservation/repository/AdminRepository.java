package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.AdminLevel;
import com.sijan.barberReservation.model.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByAdminLevel(AdminLevel adminLevel);
    Admin findByEmail(String adminEmail);
    List<Admin> findAllByAdminLevel(AdminLevel adminLevel);
    Optional<Admin> findByBarbershop(Barbershop barbershop);
}

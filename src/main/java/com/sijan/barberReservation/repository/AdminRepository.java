package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.AdminLevel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByAdminLevel(AdminLevel adminLevel);
    Admin findByAdminLevel(AdminLevel adminLevel);
    long countByAdminLevel(AdminLevel adminLevel);
}

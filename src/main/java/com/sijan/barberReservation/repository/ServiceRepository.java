package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.DTO.service.ServiceDTO;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffering, Long> {
    Page<ServiceOffering> findByBarbershop(Barbershop barberShop, Pageable pageable);


    @Query("""
       SELECT s FROM Appointment a 
       JOIN a.services s 
       WHERE a.barbershop = :shop 
       GROUP BY s 
       ORDER BY COUNT(s) DESC
       """)
    List<ServiceOffering> findPopularServices(@Param("shop") Barbershop shop, Pageable pageable);
}

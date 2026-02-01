package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceOffering, Long> {
}

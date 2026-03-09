package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}

package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}

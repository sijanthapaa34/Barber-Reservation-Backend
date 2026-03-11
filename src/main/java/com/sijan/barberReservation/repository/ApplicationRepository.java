package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Application;
import com.sijan.barberReservation.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("SELECT a FROM Application a WHERE " +
            "(a.type = 'BARBER_SHOP' AND a.status = 'PENDING') OR " +
            "(a.type = 'BARBER' AND a.status = 'PENDING_MAIN_APPROVAL')")
    Page<Application> findRelevantForMainAdmin(Pageable pageable);

    Page<Application> findByBarbershopIdAndStatus(Long barbershopId, ApplicationStatus applicationStatus, Pageable pageable);
}

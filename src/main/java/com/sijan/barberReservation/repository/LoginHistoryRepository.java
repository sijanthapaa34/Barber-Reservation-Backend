package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}

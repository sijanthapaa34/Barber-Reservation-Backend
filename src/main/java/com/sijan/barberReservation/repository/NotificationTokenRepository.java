package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {
    Optional<NotificationToken> findByToken(String token);
    List<NotificationToken> findByUserIdAndUserType(Long userId, String userType);
}

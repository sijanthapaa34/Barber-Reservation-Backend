// src/main/java/com/sijan/barberReservation/repository/NotificationRepository.java
package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.timestamp DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    long countByUserIdAndIsReadFalse(Long userId);
}

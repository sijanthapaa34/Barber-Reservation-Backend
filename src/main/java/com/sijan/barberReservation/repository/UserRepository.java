package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);

    int countByLastLoginAfter(LocalDateTime localDateTime);
}

package com.sijan.barberReservation.repository;


import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findTop5ByRoleOrderByTotalBookingsDesc(Roles role);

    Optional<Customer> findByEmail(String email);
}


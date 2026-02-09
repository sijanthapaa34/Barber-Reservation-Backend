package com.sijan.barberReservation.repository;


import com.sijan.barberReservation.model.Admin;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("""
        SELECT c
        FROM Appointment a
        JOIN a.customer c
        WHERE c.shop.admin = :admin
        GROUP BY c
        ORDER BY COUNT(a.id) DESC
    """)
    Page<Customer> findFrequentCustomersByAdmin(
            @Param("admin") Admin admin,
            Pageable pageable
    );
}


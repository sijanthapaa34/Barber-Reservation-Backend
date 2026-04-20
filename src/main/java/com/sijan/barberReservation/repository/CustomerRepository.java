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
    WHERE a.barbershop.admin = :admin
    GROUP BY c
    ORDER BY COUNT(a.id) DESC
""")
    Page<Customer> findFrequentCustomersByAdmin(
            @Param("admin") Admin admin,
            Pageable pageable
    );

    // Find customers with minimum number of bookings
    Page<Customer> findByTotalBookingsGreaterThanEqual(Integer minBookings, Pageable pageable);

    // Find all customers who have booked at a specific shop (JPQL)
    @Query("""
        SELECT DISTINCT c
        FROM Appointment a
        JOIN a.customer c
        WHERE a.barbershop.id = :shopId
        ORDER BY c.totalBookings DESC
        """)
    Page<Customer> findCustomersByShopId(@Param("shopId") Long shopId, Pageable pageable);

    // Find regular customers (3+ bookings) at a specific shop (JPQL)
    @Query("""
        SELECT c
        FROM Appointment a
        JOIN a.customer c
        WHERE a.barbershop.id = :shopId
        GROUP BY c
        HAVING COUNT(a.id) >= :minBookings
        ORDER BY COUNT(a.id) DESC
        """)
    Page<Customer> findRegularCustomersByShopId(
            @Param("shopId") Long shopId,
            @Param("minBookings") Integer minBookings,
            Pageable pageable
    );
}


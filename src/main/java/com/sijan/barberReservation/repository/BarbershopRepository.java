package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barbershop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BarbershopRepository extends JpaRepository<Barbershop, Long> {
    List<Barbershop> findByLatitudeBetweenAndLongitudeBetween(BigDecimal latMin, BigDecimal latMax, BigDecimal lonMin, BigDecimal lonMax);

    @Query("SELECT b FROM Barbershop b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :word, '%')) " +
            "OR LOWER(b.city) LIKE LOWER(CONCAT('%', :word, '%'))")
    Page<Barbershop> searchByKeyword(@Param("word") String word, Pageable pageable);
}

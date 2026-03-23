package com.sijan.barberReservation.repository;

import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.Review;
import com.sijan.barberReservation.model.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.targetType = :type AND r.targetId = :id")
    Double findAverageRating(@Param("type") ReviewType type, @Param("id") Long id);

    long countByTargetTypeAndTargetId(ReviewType type, Long targetId);

    Page<Review> findByTargetTypeAndTargetId(ReviewType type, Long targetId, Pageable pageable);
}

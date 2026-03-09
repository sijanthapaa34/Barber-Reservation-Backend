package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.Review;
import org.hibernate.query.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    public Integer countByBarbershop(Barbershop shop) {
        return 1;
    }
//    public Page<Review> getCustomerReviews(Long userId) {
//    }
}

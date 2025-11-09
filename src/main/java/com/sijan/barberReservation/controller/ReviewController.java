//package com.sijan.barberReservation.controller;
//
//package com.sijan.barberReservation.controller;
//
//import com.sijan.barberReservation.service.ReviewService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reviews")
//@Validated
//public class ReviewController {
//
//    private final ReviewService reviewService;
//
//    public ReviewController(ReviewService reviewService) {
//        this.reviewService = reviewService;
//    }
//
//    // GET /api/reviews/me - Get all reviews written by customer
//    @GetMapping("/me")
//    public ResponseEntity<List<ReviewDTO>> getMyReviews(
//            @RequestHeader("X-User-ID") Long userId) {
//
//        List<ReviewDTO> reviews = reviewService.getCustomerReviews(userId);
//        return ResponseEntity.ok(reviews);
//    }
//
//    // POST /api/reviews - Submit a new review
//    @PostMapping
//    public ResponseEntity<ReviewDTO> submitReview(
//            @RequestHeader("X-User-ID") Long userId,
//            @Valid @RequestBody SubmitReviewDTO dto) {
//
//        ReviewDTO created = reviewService.submitReview(userId, dto);
//        return ResponseEntity.status(201).body(created);
//    }
//}

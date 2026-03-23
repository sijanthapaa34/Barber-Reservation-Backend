package com.sijan.barberReservation.controller;

import com.sijan.barberReservation.DTO.appointment.PageResponse;
import com.sijan.barberReservation.DTO.review.CreateReplyRequest;
import com.sijan.barberReservation.DTO.review.CreateReviewRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.mapper.appointment.PageMapper;
import com.sijan.barberReservation.mapper.review.ReviewMapper;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Review;
import com.sijan.barberReservation.model.ReviewType;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final CustomerService customerService;
    private final PageMapper pageMapper;

    @PostMapping("/{customerId}")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        Customer customer = customerService.findById(customerId);
        Review review = reviewMapper.toEntity(request,customer);

        ReviewDTO dto = reviewService.createReview(review);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReviewDTO>> getReviews(
            @RequestParam ReviewType type,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Review> reviews = reviewService.getReviews(type, targetId, PageRequest.of(page, size));
        return ResponseEntity.ok(pageMapper.toReviewPageResponse(reviews));
    }

    @PostMapping("/{reviewId}/reply/{replierId}")
    public ResponseEntity<ReviewDTO> replyToReview(
            @PathVariable Long reviewId,
            @PathVariable Long replierId,
            @Valid @RequestBody CreateReplyRequest request
    ) {
        Review review = reviewService.replyToReview(reviewId,replierId, request);
        return ResponseEntity.ok(reviewMapper.toDTO(review));
    }
}

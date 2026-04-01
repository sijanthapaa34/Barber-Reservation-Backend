package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.review.CreateReplyRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.exception.role.AccessDeniedException;
import com.sijan.barberReservation.mapper.review.ReviewMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final EmailService emailService;
    private final ServiceOfferingService serviceService;
    private final UserService userService;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    @Transactional
    public ReviewDTO createReview(Review review) {

        if (review.getTargetType() == ReviewType.SERVICE) {
            // Service reviews cannot have ratings
            if (review.getRating() != null) {
                throw new RuntimeException("Service reviews cannot have star ratings.");
            }
        } else {
            // Barber and Shop reviews MUST have ratings
            if (review.getRating() == null) {
                throw new RuntimeException("Star rating is required for Barber and Shop reviews.");
            }
        }

        Review saved = reviewRepository.save(review);

        // Update aggregate rating (Only for Barber and Shop)
        if (review.getTargetType() != ReviewType.SERVICE) {
            updateTargetRating(review.getTargetType(), review.getTargetId());
        }

        // Send Email Notification (Only for Barber for now)
        if (saved.getTargetType() == ReviewType.BARBER) {
            Barber barber = barberService.findById(saved.getTargetId());

            emailService.sendNewReviewNotification(
                    barber.getEmail(),
                    barber.getName(),
                    saved.getCustomer().getName(),
                    saved.getRating(),
                    saved.getComment()
            );

            notificationService.sendReviewSubmittedToBarber(
                    barber.getId(),
                    saved.getCustomer().getName(),
                    saved.getRating()
            );
        }

        return reviewMapper.toDTO(saved);
    }

    private void updateTargetRating(ReviewType type, Long targetId) {
        Double avgRating = reviewRepository.findAverageRating(type, targetId);
        long count = reviewRepository.countByTargetTypeAndTargetId(type, targetId);

        double rating = avgRating != null ? avgRating : 0.0;

        if (type == ReviewType.BARBER) {
            Barber barber = barberService.findById(targetId);
            barber.setRating(rating);
            barber.setReviewCount((int) count);
            barberService.saveRating(barber);
        } else if (type == ReviewType.BARBER_SHOP) {
            Barbershop shop = barbershopService.findById(targetId);
            shop.setRating(rating);
            shop.setReviewCount((int) count);
            barbershopService.saveRating(shop);
        }
        // No case for SERVICE
    }

    public Page<Review> getReviews(ReviewType type, Long targetId, Pageable pageable) {
        return reviewRepository.findByTargetTypeAndTargetId(type, targetId, pageable);
//                .map(reviewMapper::toDTO);
    }

    @Transactional
    public Review replyToReview(Long reviewId, Long userId, CreateReplyRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        User user = userService.findById(userId);

        // Authorization check
        boolean canReply = false;

        // Main Admin can always reply
        if (user.getRole() == Roles.MAIN_ADMIN) {
            canReply = true;
        }
        // Shop Admin can reply to their shop's reviews or their shop's barber's reviews
        else if (user instanceof Admin) {
            Admin admin = (Admin) user;
            Long shopId = admin.getBarbershop().getId();

            if (review.getTargetType() == ReviewType.BARBER_SHOP && review.getTargetId().equals(shopId)) {
                canReply = true; // Replying to shop review
            } else if (review.getTargetType() == ReviewType.BARBER) {
                // Check if this barber belongs to the admin's shop
                Barber barber = barberService.findById(review.getTargetId());
                if (barber != null && barber.getBarbershop().getId().equals(shopId)) {
                    canReply = true; // Replying to barber review
                }
            } else if (review.getTargetType() == ReviewType.SERVICE) {
                // Check if service belongs to admin's shop
                // (Implementation depends on your Service repository, assuming you have serviceRepository)
                 ServiceOffering service = serviceService.findById(review.getTargetId());
                 if (service != null && service.getBarbershop().getId().equals(shopId)) canReply = true;
            }
        }
        // Barber can reply to their own reviews
        else if (user instanceof Barber) {
            if (review.getTargetType() == ReviewType.BARBER && review.getTargetId().equals(user.getId())) {
                canReply = true;
            }
        }

        if (!canReply) {
            throw new AccessDeniedException("You are not authorized to reply to this review");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setUser(user);
        reply.setComment(request.getComment());

        review.getReplies().add(reply);
//
//        notificationService.sendReviewReplyToCustomer(
//                customer.getId(),
//                barber.getName(),
//                barbershop.getName()
//        );

        return reviewRepository.save(review);
    }

    public Long countByBarbershop(Barbershop shop) {
        return reviewRepository.countByTargetTypeAndTargetId(ReviewType.BARBER_SHOP, shop.getId());
    }
}
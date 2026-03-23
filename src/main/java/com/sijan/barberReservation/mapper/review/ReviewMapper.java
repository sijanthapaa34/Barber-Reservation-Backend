package com.sijan.barberReservation.mapper.review;

import com.sijan.barberReservation.DTO.review.CreateReviewRequest;
import com.sijan.barberReservation.DTO.review.ReviewDTO;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.Review;
import com.sijan.barberReservation.model.ReviewReply;
import com.sijan.barberReservation.model.ReviewType;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    public ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());

        dto.setCustomerId(review.getCustomer().getId());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setCustomerProfilePic(review.getCustomer().getProfilePicture());

        dto.setTargetType(review.getTargetType());
        dto.setTargetId(review.getTargetId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setImageUrl(review.getImageUrl());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getReplies() != null) {
            dto.setReplies(review.getReplies().stream().map(this::toReplyDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private ReviewDTO.ReplyDTO toReplyDTO(ReviewReply reply) {
        ReviewDTO.ReplyDTO dto = new ReviewDTO.ReplyDTO();
        dto.setId(reply.getId());
        dto.setUserId(reply.getUser().getId());
        dto.setUserName(reply.getUser().getName());
        dto.setUserRole(reply.getUser().getRole().name());
        dto.setComment(reply.getComment());
        dto.setCreatedAt(reply.getCreatedAt());
        return dto;
    }

    public Review toEntity(@Valid CreateReviewRequest request, Customer customer) {
        Review review = new Review();
        review.setCustomer(customer);

        review.setTargetType(request.getTargetType());
        review.setTargetId(request.getTargetId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setImageUrl(request.getImageUrl());

        return review;
    }
}

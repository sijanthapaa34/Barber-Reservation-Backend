package com.sijan.barberReservation.DTO.review;

import com.sijan.barberReservation.model.ReviewType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDTO {
    private Long id;

    private Long customerId;
    private String customerName;
    private String customerProfilePic;

    private ReviewType targetType;
    private Long targetId;

    private Integer rating;
    private String comment;
    private String imageUrl;
    private LocalDateTime createdAt;

    private List<ReplyDTO> replies;

    @Data
    public static class ReplyDTO {
        private Long id;
        private Long userId;
        private String userName;
        private String userRole;
        private String comment;
        private LocalDateTime createdAt;
    }
}
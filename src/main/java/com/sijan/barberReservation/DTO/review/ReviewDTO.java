package com.sijan.barberReservation.DTO.review;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDTO {

    private Long id;

    private Long customerId;
    private String customerName;

    private int rating;
    private String comment;

    private String targetType;
    private Long targetId;

    private List<String> images;

    private LocalDateTime date;
}
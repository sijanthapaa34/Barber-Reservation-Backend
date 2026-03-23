package com.sijan.barberReservation.DTO.review;

import com.sijan.barberReservation.model.ReviewType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateReviewRequest {
    @NotNull
    private Long targetId;

    @NotNull
    private ReviewType targetType;

    @Min(1) @Max(5)
    private Integer rating;

    @NotBlank
    @Size(max = 1000)
    private String comment;

    private String imageUrl;
}

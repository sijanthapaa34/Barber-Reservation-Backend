package com.sijan.barberReservation.DTO.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReplyRequest {
    @NotBlank
    @Size(max = 1000)
    private String comment;
}
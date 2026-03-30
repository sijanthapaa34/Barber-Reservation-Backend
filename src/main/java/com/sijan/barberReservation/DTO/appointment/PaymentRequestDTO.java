package com.sijan.barberReservation.DTO.appointment;

import com.sijan.barberReservation.model.PaymentMethod;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentRequestDTO {
    @NotNull
    private Long barberId;
    @NotNull
    private Long barbershopId;
    @NotNull
    @Size(min = 1)
    private List<Long> serviceIds;
    @NotNull
    @Future
    private LocalDateTime scheduledTime;
    private PaymentMethod paymentMethod;
}
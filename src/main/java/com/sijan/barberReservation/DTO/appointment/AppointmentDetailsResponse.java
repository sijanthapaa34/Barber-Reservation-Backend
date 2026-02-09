package com.sijan.barberReservation.DTO.appointment;

import com.sijan.barberReservation.model.AppointmentStatus;
import com.sijan.barberReservation.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDetailsResponse {

    private Long appointmentId;

    private Long customerId;
    private String customerName;

    private Long barberId;
    private String barberName;

    private Long barbershopId;
    private String barbershopName;

    private List<ServiceItemDTO> services;

    private Double totalPrice;
    private Integer totalDurationMinutes;

    private AppointmentStatus status;

    private LocalDateTime scheduledTime;
    private LocalDateTime checkInTime;
    private LocalDateTime completedTime;

    private PaymentStatus paymentStatus;
    private Double paidAmount;
    private String paymentMethod;

    private String customerNotes;
    private String barberNotes;

    private LocalDateTime createdAt;
}


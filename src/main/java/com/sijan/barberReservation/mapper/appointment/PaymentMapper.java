package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.model.PaymentTransaction;
import com.sijan.barberReservation.model.TransactionStatus;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.ServiceOfferingService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class PaymentMapper {
    private final BarberService barberService;
    private final BarbershopService barbershopService;
    private final ServiceOfferingService serviceOfferingService;

    public PaymentTransaction toEntity(PaymentRequestDTO dto) {
        PaymentTransaction tx = new PaymentTransaction();

        tx.setStatus(TransactionStatus.PENDING);
        tx.setPaymentMethod(dto.getPaymentMethod());
        tx.setBarber(barberService.findById(dto.getBarberId()));
        tx.setBarbershop(barbershopService.findById(dto.getBarbershopId()));
        tx.setScheduledTime(dto.getScheduledTime());
        tx.setServices(serviceOfferingService.findByIds(dto.getServiceIds()));

        return tx;
    }
}

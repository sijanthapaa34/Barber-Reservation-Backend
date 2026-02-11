package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.CreateAppointmentRequest;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.AppointmentStatus;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.Barbershop;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateAppointmentMapper {

    private final com.sijan.barberReservation.service.BarberService barberService;
    private final CustomerService customerService;
    private final BarbershopService barbershopService;
    private final ServiceOfferingService servicesService;

    public CreateAppointmentMapper(BarberService barberService, CustomerService customerService, BarbershopService barbershopService, ServiceOfferingService servicesService) {
        this.barberService = barberService;
        this.customerService = customerService;
        this.barbershopService = barbershopService;
        this.servicesService = servicesService;
    }


    public Appointment toAppointment(CreateAppointmentRequest request){
        Barber barber = barberService.findById(request.getBarberId());
        List<ServiceOffering> service = servicesService.findByIds(request.getServiceIds());
        Barbershop barbershop = barbershopService.findById(request.getBarbershopId());

        LocalDateTime appointmentDateTime = request.getScheduledTime();
        LocalDateTime checkInTime = appointmentDateTime.minusMinutes(10);

        Appointment appointment = new Appointment();
        appointment.setBarber(barber);
        appointment.setBarbershop(barbershop);
        appointment.setServices(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCheckInTime(checkInTime);
        appointment.setScheduledTime(appointmentDateTime);
        appointment.setCreatedAt(LocalDateTime.now());

        return appointment;
    }
}

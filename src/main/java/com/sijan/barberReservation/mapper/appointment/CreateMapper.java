package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.CreateAppointmentRequest;
import com.sijan.barberReservation.model.Appointment;
import com.sijan.barberReservation.model.AppointmentStatus;
import com.sijan.barberReservation.model.Barber;
import com.sijan.barberReservation.model.BarberShop;
import com.sijan.barberReservation.model.Customer;
import com.sijan.barberReservation.model.ServiceOffering;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarberShopService;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateMapper {

    private final com.sijan.barberReservation.service.BarberService barberService;
    private final CustomerService customerService;
    private final BarberShopService barberShopService;
    private final ServiceOfferingService servicesService;

    public CreateMapper(BarberService barberService, CustomerService customerService, BarberShopService barberShopService, ServiceOfferingService servicesService) {
        this.barberService = barberService;
        this.customerService = customerService;
        this.barberShopService = barberShopService;
        this.servicesService = servicesService;
    }


    public Appointment toAppointment(CreateAppointmentRequest request){
        Barber barber = barberService.findById(request.getBarberId());
        Customer customer = customerService.findById(request.getCustomerId());
        List<ServiceOffering> service = servicesService.findByIds(request.getServiceId());
        BarberShop barbershop = barberShopService.findById(request.getBarberShopId());

        LocalDateTime appointmentDateTime = request.getAppointmentDateTime();
        LocalDateTime checkInTime = appointmentDateTime.minusMinutes(10);

        Appointment appointment = new Appointment();
        appointment.setBarber(barber);
        appointment.setBarbershop(barbershop);
        appointment.setCustomer(customer);
        appointment.setServices(service);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCheckInTime(checkInTime);
        appointment.setScheduledTime(appointmentDateTime);
        appointment.setCreatedAt(LocalDateTime.now());

        return appointment;
    }
}

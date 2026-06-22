package com.sijan.barberReservation.mapper.appointment;

import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.service.BarberService;
import com.sijan.barberReservation.service.BarbershopService;
import com.sijan.barberReservation.service.ServiceOfferingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAppointmentMapperTest {

    @Mock
    private BarberService barberService;

    @Mock
    private BarbershopService barbershopService;

    @Mock
    private ServiceOfferingService servicesService;

    @InjectMocks
    private CreateAppointmentMapper createAppointmentMapper;

    private Barber mockBarber;
    private Barbershop mockBarbershop;
    private List<ServiceOffering> mockServices;

    @BeforeEach
    void setUp() {
        mockBarber = new Barber();
        mockBarber.setId(1L);
        mockBarber.setName("Test Barber");

        mockBarbershop = new Barbershop();
        mockBarbershop.setId(1L);
        mockBarbershop.setName("Test Shop");

        ServiceOffering service1 = new ServiceOffering();
        service1.setId(1L);
        service1.setName("Haircut");
        service1.setPrice(25.0);
        service1.setDurationMinutes(30);

        ServiceOffering service2 = new ServiceOffering();
        service2.setId(2L);
        service2.setName("Beard Trim");
        service2.setPrice(15.0);
        service2.setDurationMinutes(20);

        mockServices = Arrays.asList(service1, service2);
    }

    @Test
    void toAppointment_WithCompleteRequest_ShouldMapAllFields() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L, 2L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 6, 15, 10, 0));
        request.setPaymentMethod(PaymentMethod.KHALTI);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L, 2L))).thenReturn(mockServices);
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertNotNull(appointment);
        assertEquals(mockBarber, appointment.getBarber());
        assertEquals(mockBarbershop, appointment.getBarbershop());
        assertEquals(mockServices, appointment.getServices());
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
        assertEquals(LocalDateTime.of(2023, 6, 15, 10, 0), appointment.getScheduledTime());
        assertEquals(PaymentMethod.KHALTI, appointment.getPaymentMethod());

        verify(barberService, times(1)).findById(1L);
        verify(servicesService, times(1)).findByIds(Arrays.asList(1L, 2L));
        verify(barbershopService, times(1)).findById(1L);
    }

    @Test
    void toAppointment_WithEsewaPayment_ShouldSetCorrectPaymentMethod() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 7, 20, 14, 30));
        request.setPaymentMethod(PaymentMethod.ESEWA);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L))).thenReturn(Arrays.asList(mockServices.get(0)));
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertNotNull(appointment);
        assertEquals(PaymentMethod.ESEWA, appointment.getPaymentMethod());
    }

    @Test
    void toAppointment_WithCashPayment_ShouldSetCorrectPaymentMethod() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 8, 10, 9, 0));
        request.setPaymentMethod(PaymentMethod.CASH);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L))).thenReturn(Arrays.asList(mockServices.get(0)));
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertNotNull(appointment);
        assertEquals(PaymentMethod.CASH, appointment.getPaymentMethod());
    }

    @Test
    void toAppointment_ShouldAlwaysSetStatusToScheduled() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L, 2L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 9, 5, 11, 30));
        request.setPaymentMethod(PaymentMethod.KHALTI);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L, 2L))).thenReturn(mockServices);
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
    }

    @Test
    void toAppointment_WithMultipleServices_ShouldMapAllServices() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L, 2L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 10, 1, 15, 0));
        request.setPaymentMethod(PaymentMethod.KHALTI);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L, 2L))).thenReturn(mockServices);
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertNotNull(appointment.getServices());
        assertEquals(2, appointment.getServices().size());
        assertEquals("Haircut", appointment.getServices().get(0).getName());
        assertEquals("Beard Trim", appointment.getServices().get(1).getName());
    }

    @Test
    void toAppointment_WithSingleService_ShouldMapCorrectly() {
        // Arrange
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setServiceIds(Arrays.asList(1L));
        request.setBarbershopId(1L);
        request.setScheduledTime(LocalDateTime.of(2023, 11, 15, 16, 30));
        request.setPaymentMethod(PaymentMethod.ESEWA);

        when(barberService.findById(1L)).thenReturn(mockBarber);
        when(servicesService.findByIds(Arrays.asList(1L))).thenReturn(Arrays.asList(mockServices.get(0)));
        when(barbershopService.findById(1L)).thenReturn(mockBarbershop);

        // Act
        Appointment appointment = createAppointmentMapper.toAppointment(request);

        // Assert
        assertNotNull(appointment.getServices());
        assertEquals(1, appointment.getServices().size());
        assertEquals("Haircut", appointment.getServices().get(0).getName());
    }
}

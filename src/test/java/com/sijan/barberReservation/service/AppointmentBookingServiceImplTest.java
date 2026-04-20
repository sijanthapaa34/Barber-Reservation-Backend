package com.sijan.barberReservation.service;

import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.AdminRepository;
import com.sijan.barberReservation.repository.AppointmentRepository;
import com.sijan.barberReservation.repository.CustomerRepository;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AppointmentBookingServiceImpl bookingService;

    private PaymentTransaction testTransaction;
    private Customer testCustomer;
    private Barber testBarber;
    private Barbershop testShop;
    private ServiceOffering testService;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("customer@test.com");
        testCustomer.setPoints(0);
        testCustomer.setTotalBookings(0);

        testShop = new Barbershop();
        testShop.setId(1L);
        testShop.setName("Test Shop");
        testShop.setBalance(BigDecimal.ZERO);

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setName("Test Barber");
        testBarber.setEmail("barber@test.com");
        testBarber.setBarbershop(testShop);
        testBarber.setBalance(BigDecimal.ZERO);

        testService = new ServiceOffering();
        testService.setId(1L);
        testService.setName("Haircut");
        testService.setDurationMinutes(30);
        testService.setPrice(200.0);

        testTransaction = new PaymentTransaction();
        testTransaction.setId(1L);
        testTransaction.setCustomer(testCustomer);
        testTransaction.setBarber(testBarber);
        testTransaction.setBarbershop(testShop);
        testTransaction.setScheduledTime(LocalDateTime.now().plusDays(1));
        testTransaction.setAmount(BigDecimal.valueOf(200));
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);
        testTransaction.setServices(Arrays.asList(testService));

        // Inject the mocked EntityManager using ReflectionTestUtils
        ReflectionTestUtils.setField(bookingService, "entityManager", entityManager);
    }

    @Test
    void bookPaidAppointment_ValidTransaction_CreatesAppointment() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        Appointment result = bookingService.bookPaidAppointment(testTransaction);

        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        assertEquals(testBarber, result.getBarber());
        assertEquals(AppointmentStatus.SCHEDULED, result.getStatus());
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());
        assertEquals(30, result.getTotalDurationMinutes());
        assertEquals(200.0, result.getTotalPrice(), 0.01);

        verify(appointmentRepository, times(2)).save(any(Appointment.class));
        verify(transactionRepository, times(1)).save(testTransaction);
    }

    @Test
    void bookPaidAppointment_AwardsLoyaltyPoints() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        bookingService.bookPaidAppointment(testTransaction);

        // Rs. 200 = 2 points
        assertEquals(2, testCustomer.getPoints());
        verify(customerRepository, times(2)).save(testCustomer); // Once for points, once for bookings
    }

    @Test
    void bookPaidAppointment_IncrementsTotalBookings() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        bookingService.bookPaidAppointment(testTransaction);

        assertEquals(1, testCustomer.getTotalBookings());
        verify(customerRepository, times(2)).save(testCustomer);
    }

    @Test
    void bookPaidAppointment_DistributesEarnings() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        bookingService.bookPaidAppointment(testTransaction);

        // Platform fee = 5% of 200 = 10
        // Shop earnings = 200 - 10 = 190
        assertEquals(new BigDecimal("10.00"), testTransaction.getPlatformFee());
        assertEquals(new BigDecimal("190.00"), testTransaction.getShopEarnings());
        assertEquals(new BigDecimal("190.00"), testShop.getBalance());
        assertEquals(new BigDecimal("190.00"), testBarber.getBalance());
    }

    @Test
    void bookPaidAppointment_SendsNotifications() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        bookingService.bookPaidAppointment(testTransaction);

        verify(notificationService, times(1)).sendAppointmentBookedToCustomer(
                anyLong(), anyString(), anyString(), anyString());
        verify(notificationService, times(1)).sendNewAppointmentToBarber(
                anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void bookPaidAppointment_MultipleServices_CalculatesCorrectDuration() {
        ServiceOffering service2 = new ServiceOffering();
        service2.setId(2L);
        service2.setName("Shave");
        service2.setDurationMinutes(15);
        service2.setPrice(100.0);

        testTransaction.setServices(Arrays.asList(testService, service2));
        testTransaction.setAmount(BigDecimal.valueOf(300));

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment apt = invocation.getArgument(0);
            apt.setId(1L);
            return apt;
        });
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
        when(adminRepository.findByBarbershop(any(Barbershop.class))).thenReturn(Optional.empty());
        doNothing().when(entityManager).flush();

        Appointment result = bookingService.bookPaidAppointment(testTransaction);

        assertEquals(45, result.getTotalDurationMinutes()); // 30 + 15
        assertEquals(300.0, result.getTotalPrice(), 0.01); // 200 + 100
    }
}

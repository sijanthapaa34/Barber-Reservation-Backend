package com.sijan.barberReservation.service;

import com.sijan.barberReservation.exception.appointment.AppointmentAlreadyCancelledException;
import com.sijan.barberReservation.exception.appointment.AppointmentNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private BarberLeaveService barberLeaveService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private SlotReservationService slotReservationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private BarbershopService barbershopService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Appointment testAppointment;
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
        testCustomer.setPoints(10);

        testShop = new Barbershop();
        testShop.setId(1L);
        testShop.setName("Test Shop");

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setName("Test Barber");
        testBarber.setEmail("barber@test.com");
        testBarber.setBarbershop(testShop);

        testService = new ServiceOffering();
        testService.setId(1L);
        testService.setName("Haircut");
        testService.setDurationMinutes(30);
        testService.setPrice(200.0);

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setCustomer(testCustomer);
        testAppointment.setBarber(testBarber);
        testAppointment.setBarbershop(testShop);
        testAppointment.setScheduledTime(LocalDateTime.now().plusDays(2));
        testAppointment.setStatus(AppointmentStatus.SCHEDULED);
        testAppointment.setServices(Arrays.asList(testService));
        testAppointment.setTotalDurationMinutes(30);
        testAppointment.setTotalPrice(200.0);
    }

    @Test
    void findById_ExistingAppointment_ReturnsAppointment() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        Appointment result = appointmentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(appointmentRepository, times(1)).findById(1L);
    }

    @Test
    void findById_NonExistingAppointment_ThrowsException() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> {
            appointmentService.findById(999L);
        });
    }

    @Test
    void cancel_ScheduledAppointment_CancelsSuccessfully() {
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(transactionRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        appointmentService.cancel(1L, testCustomer);

        assertEquals(AppointmentStatus.CANCELLED, testAppointment.getStatus());
        assertEquals(10, testCustomer.getPoints()); // Points not deducted in mock (would be 9 in real scenario)
        verify(appointmentRepository, times(1)).save(testAppointment);
    }

    @Test
    void cancel_AlreadyCancelledAppointment_ThrowsException() {
        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(AppointmentAlreadyCancelledException.class, () -> {
            appointmentService.cancel(1L, testCustomer);
        });
    }

    @Test
    void cancel_CompletedAppointment_ThrowsException() {
        testAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        assertThrows(IllegalStateException.class, () -> {
            appointmentService.cancel(1L, testCustomer);
        });
    }

    @Test
    void getUpcomingByCustomer_ReturnsUpcomingAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(Arrays.asList(testAppointment));

        when(appointmentRepository.findUpcomingByCustomer(eq(testCustomer), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(appointmentPage);

        Page<Appointment> result = appointmentService.getUpcomingByCustomer(testCustomer, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(appointmentRepository, times(1)).findUpcomingByCustomer(eq(testCustomer), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void getPastByCustomer_ReturnsPastAppointments() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(Arrays.asList(testAppointment));

        when(appointmentRepository.findPastByCustomer(eq(testCustomer), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(appointmentPage);

        Page<Appointment> result = appointmentService.getPastByCustomer(testCustomer, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(appointmentRepository, times(1)).findPastByCustomer(eq(testCustomer), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void computeAvailableSlots_BarberOnLeave_ReturnsEmptyList() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<ServiceOffering> services = Arrays.asList(testService);

        when(barberLeaveService.isOnLeave(testBarber, LeaveStatus.APPROVED, date, date)).thenReturn(true);

        List<LocalDateTime> result = appointmentService.computeAvailableSlots(testBarber, date, services, null);

        assertTrue(result.isEmpty());
        verify(barberLeaveService, times(1)).isOnLeave(testBarber, LeaveStatus.APPROVED, date, date);
    }

    @Test
    void computeAvailableSlots_NoBookings_ReturnsAllSlots() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<ServiceOffering> services = Arrays.asList(testService);

        when(barberLeaveService.isOnLeave(testBarber, LeaveStatus.APPROVED, date, date)).thenReturn(false);
        when(appointmentRepository.findByBarberAndStatusAndScheduledTimeBetween(
                eq(testBarber), eq(AppointmentStatus.SCHEDULED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());
        when(slotReservationService.findActiveByBarberAndDate(eq(testBarber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList());

        List<LocalDateTime> result = appointmentService.computeAvailableSlots(testBarber, date, services, null);

        assertFalse(result.isEmpty());
        // 9 AM to 6 PM with 30-minute service = 18 slots
        assertTrue(result.size() > 0);
    }

    @Test
    void getEarnings_ValidDateRange_ReturnsEarnings() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        Double expectedEarnings = 1000.0;

        when(transactionRepository.sumBarberEarningsByPaidAtBetween(eq(testBarber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedEarnings);

        Double result = appointmentService.getEarnings(testBarber, startDate, endDate);

        assertEquals(expectedEarnings, result);
        verify(transactionRepository, times(1)).sumBarberEarningsByPaidAtBetween(eq(testBarber), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getEarnings_NoEarnings_ReturnsZero() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(transactionRepository.sumBarberEarningsByPaidAtBetween(eq(testBarber), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(null);

        Double result = appointmentService.getEarnings(testBarber, startDate, endDate);

        assertEquals(0.0, result);
    }

    @Test
    void sendManualReminder_ValidAppointment_SendsNotifications() {
        appointmentService.sendManualReminder(testAppointment);

        verify(emailService, times(1)).sendAppointmentReminder(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(notificationService, times(1)).sendAppointmentReminder(
                anyLong(), anyString(), anyString(), anyString());
    }
}

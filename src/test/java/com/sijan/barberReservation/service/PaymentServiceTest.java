package com.sijan.barberReservation.service;

import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.exception.appointment.AppointmentSlotUnavailableException;
import com.sijan.barberReservation.exception.role.ResourceNotFoundException;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private AppointmentBookingService appointmentService;

    @Mock
    private KhaltiService khaltiService;

    @Mock
    private EsewaService esewaService;

    @Mock
    private SlotReservationService slotReservationService;

    @Mock
    private BarbershopRepository barbershopRepository;

    @Mock
    private BarberRepository barberRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentTransaction testTransaction;
    private Customer testCustomer;
    private Barbershop testBarbershop;
    private Barber testBarber;
    private ServiceOffering testService;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");

        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");
        testBarbershop.setBalance(new BigDecimal("1000.00"));

        testBarber = new Barber();
        testBarber.setId(1L);
        testBarber.setName("Test Barber");
        testBarber.setBarbershop(testBarbershop);
        testBarber.setBalance(new BigDecimal("500.00"));

        testService = new ServiceOffering();
        testService.setId(1L);
        testService.setName("Haircut");
        testService.setPrice(50.0);

        testTransaction = new PaymentTransaction();
        testTransaction.setId(1L);
        testTransaction.setCustomer(testCustomer);
        testTransaction.setBarbershop(testBarbershop);
        testTransaction.setBarber(testBarber);
        testTransaction.setServices(Arrays.asList(testService));
        testTransaction.setScheduledTime(LocalDateTime.now().plusDays(1));
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);
        testTransaction.setStatus(TransactionStatus.PENDING);
        testTransaction.setAmount(new BigDecimal("50.00"));

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setCustomer(testCustomer);
        testAppointment.setBarbershop(testBarbershop);
    }

    @Test
    void initiatePayment_Khalti_Success() throws Exception {
        Map<String, Object> khaltiResponse = new HashMap<>();
        khaltiResponse.put("payment_url", "https://khalti.com/pay");
        khaltiResponse.put("pidx", "test-pidx");

        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(khaltiService.initiatePayment(anyLong(), any(), anyString())).thenReturn(khaltiResponse);

        PaymentInitiationResponse response = paymentService.initiatePayment(testTransaction, testCustomer);

        assertNotNull(response);
        assertEquals("https://khalti.com/pay", response.getPaymentUrl());
        assertEquals("KHALTI", response.getPaymentMethod());
        assertEquals("test-pidx", response.getPidx());
        verify(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        verify(khaltiService).initiatePayment(anyLong(), any(), anyString());
    }

    @Test
    void initiatePayment_Esewa_Success() throws Exception {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        Map<String, String> esewaData = new HashMap<>();
        esewaData.put("payment_url", "https://esewa.com/pay");
        esewaData.put("transaction_uuid", "test-uuid");

        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(esewaService.preparePaymentData(anyLong(), any())).thenReturn(esewaData);

        PaymentInitiationResponse response = paymentService.initiatePayment(testTransaction, testCustomer);

        assertNotNull(response);
        assertEquals("https://esewa.com/pay", response.getPaymentUrl());
        assertEquals("ESEWA", response.getPaymentMethod());
        verify(esewaService).preparePaymentData(anyLong(), any());
    }

    @Test
    void initiatePayment_NoServices_ThrowsException() {
        testTransaction.setServices(Arrays.asList());

        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.initiatePayment(testTransaction, testCustomer);
        });
    }

    @Test
    void initiatePayment_SlotUnavailable_ThrowsException() throws Exception {
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doThrow(new AppointmentSlotUnavailableException("Slot unavailable"))
                .when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());

        assertThrows(AppointmentSlotUnavailableException.class, () -> {
            paymentService.initiatePayment(testTransaction, testCustomer);
        });

        verify(transactionRepository, times(2)).save(any());
    }

    @Test
    void verifyAndConfirmPayment_Khalti_Success() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");
        request.setGatewayTransactionId("gateway-123");

        testTransaction.setPidx("test-pidx");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(khaltiService.verifyPayment("test-pidx")).thenReturn(true);
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);
        doNothing().when(notificationService).sendPaymentCompletedToCustomer(anyLong(), anyString(), anyString(), anyString());

        Appointment result = paymentService.verifyAndConfirmPayment(request);

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        verify(khaltiService).verifyPayment("test-pidx");
        verify(appointmentService).bookPaidAppointment(any());
    }

    @Test
    void verifyAndConfirmPayment_Esewa_Success() {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId("test-ref-id");

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setRefId("test-ref-id");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(esewaService.verifyPayment(anyString(), anyLong(), any())).thenReturn(true);
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);
        doNothing().when(notificationService).sendPaymentCompletedToCustomer(anyLong(), anyString(), anyString(), anyString());

        Appointment result = paymentService.verifyAndConfirmPayment(request);

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        verify(esewaService).verifyPayment(anyString(), anyLong(), any());
    }

    @Test
    void verifyAndConfirmPayment_AlreadyCompleted_ReturnsAppointment() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setAppointment(testAppointment);

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));

        Appointment result = paymentService.verifyAndConfirmPayment(request);

        assertNotNull(result);
        assertEquals(testAppointment, result);
        verify(khaltiService, never()).verifyPayment(anyString());
    }

    @Test
    void verifyAndConfirmPayment_ReservationExpired_ThrowsException() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void verifyAndConfirmPayment_VerificationFailed_ThrowsException() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(khaltiService.verifyPayment("test-pidx")).thenReturn(false);
        doNothing().when(slotReservationService).cancelReservation(1L);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
        verify(slotReservationService).cancelReservation(1L);
    }

    @Test
    void processRefundForAppointment_FullRefund_Success() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPidx("test-pidx");

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
        assertEquals(TransactionStatus.REFUNDED, testTransaction.getStatus());
        assertEquals(new BigDecimal("50.00"), testTransaction.getRefundAmount());
        assertEquals(new BigDecimal("0.00"), testTransaction.getPenaltyAmount());
    }

    @Test
    void processRefundForAppointment_PartialRefund_Success() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPidx("test-pidx");

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 0.5);

        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
        assertEquals(new BigDecimal("25.00"), testTransaction.getRefundAmount());
        assertEquals(new BigDecimal("25.00"), testTransaction.getPenaltyAmount());
    }

    @Test
    void processRefundForAppointment_NoRefund_Success() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 0.0);

        assertEquals(RefundStatus.NOT_REQUIRED, testTransaction.getRefundStatus());
        assertEquals(new BigDecimal("0.00"), testTransaction.getRefundAmount());
        assertEquals(new BigDecimal("50.00"), testTransaction.getPenaltyAmount());
    }

    @Test
    void processRefundForAppointment_AlreadyCompleted_Skips() {
        testTransaction.setRefundStatus(RefundStatus.COMPLETED);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processRefundForAppointment_InvalidStatus_ThrowsException() {
        testTransaction.setStatus(TransactionStatus.PENDING);

        assertThrows(IllegalStateException.class, () -> {
            paymentService.processRefundForAppointment(testTransaction, 1.0);
        });
    }

    @Test
    void failTransaction_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.failTransaction(1L);

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void cancelPayment_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).cancelReservation(1L);

        paymentService.cancelPayment(1L);

        verify(slotReservationService).cancelReservation(1L);
        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void retryRefund_Success() {
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setRefundAmount(new BigDecimal("50.00"));
        testTransaction.setPidx("test-pidx");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(khaltiService.refundPayment(anyString(), any())).thenReturn(true);
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.retryRefund(1L);

        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
        assertEquals(TransactionStatus.REFUNDED, testTransaction.getStatus());
        verify(khaltiService).refundPayment("test-pidx", new BigDecimal("50.00"));
    }

    @Test
    void retryRefund_NotInFailedState_ThrowsException() {
        testTransaction.setRefundStatus(RefundStatus.COMPLETED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(IllegalStateException.class, () -> {
            paymentService.retryRefund(1L);
        });
    }

    @Test
    void handleExpiredPayment_Success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).cancelReservation(1L);

        paymentService.handleExpiredPayment(1L);

        verify(slotReservationService).cancelReservation(1L);
        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }
}

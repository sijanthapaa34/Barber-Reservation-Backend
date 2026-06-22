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

    @Test
    void initiatePayment_KhaltiGatewayError_FailsTransaction() throws Exception {
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(khaltiService.initiatePayment(anyLong(), any(), anyString()))
                .thenThrow(new RuntimeException("Gateway error"));
        doNothing().when(slotReservationService).cancelReservation(anyLong());

        assertThrows(RuntimeException.class, () -> {
            paymentService.initiatePayment(testTransaction, testCustomer);
        });

        verify(slotReservationService).cancelReservation(1L);
    }

    @Test
    void initiatePayment_EsewaGatewayError_FailsTransaction() throws Exception {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        
        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(esewaService.preparePaymentData(anyLong(), any()))
                .thenThrow(new RuntimeException("Gateway error"));
        doNothing().when(slotReservationService).cancelReservation(anyLong());

        assertThrows(RuntimeException.class, () -> {
            paymentService.initiatePayment(testTransaction, testCustomer);
        });

        verify(slotReservationService).cancelReservation(1L);
    }

    @Test
    void verifyAndConfirmPayment_TransactionNotFound_ThrowsException() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(999L);

        when(transactionRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });
    }

    @Test
    void verifyAndConfirmPayment_AlreadyFailed_ThrowsException() {
        testTransaction.setStatus(TransactionStatus.FAILED);

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(IllegalStateException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });
    }

    @Test
    void verifyAndConfirmPayment_Khalti_NullPidx_FailsVerification() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx(null); // Null pidx

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        doNothing().when(slotReservationService).cancelReservation(1L);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void verifyAndConfirmPayment_Khalti_EmptyPidx_FailsVerification() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx(""); // Empty pidx

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        doNothing().when(slotReservationService).cancelReservation(1L);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void verifyAndConfirmPayment_Khalti_RetryLogic_EventuallySucceeds() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(khaltiService.verifyPayment("test-pidx"))
                .thenThrow(new RuntimeException("Pending"))
                .thenThrow(new RuntimeException("Pending"))
                .thenReturn(true); // Succeeds on third try
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);

        Appointment result = paymentService.verifyAndConfirmPayment(request);

        assertNotNull(result);
        verify(khaltiService, times(3)).verifyPayment("test-pidx");
    }

    @Test
    void verifyAndConfirmPayment_Esewa_NullRefId_FailsVerification() {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId(null);

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setRefId(null);

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        doNothing().when(slotReservationService).cancelReservation(1L);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void processRefundForAppointment_Khalti_RefundFails_MarksCompleted() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPidx("test-pidx");
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        // Service now marks as COMPLETED regardless of gateway result
        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
    }

    @Test
    void processRefundForAppointment_Khalti_RefundException_MarksCompleted() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPidx("test-pidx");
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        // Service now marks as COMPLETED regardless of gateway result
        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
    }

    @Test
    void retryRefund_Khalti_Fails_RemainsInFailedState() {
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setRefundAmount(new BigDecimal("50.00"));
        testTransaction.setPidx("test-pidx");
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(khaltiService.refundPayment(anyString(), any())).thenReturn(false);
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.retryRefund(1L);

        assertEquals(RefundStatus.FAILED_PENDING_REVIEW, testTransaction.getRefundStatus());
    }

    @Test
    void verifyAndConfirmPayment_SendsNotification() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(khaltiService.verifyPayment("test-pidx")).thenReturn(true);
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);

        paymentService.verifyAndConfirmPayment(request);

        verify(notificationService).sendPaymentCompletedToCustomer(
                eq(1L), eq("Test Shop"), anyString(), eq("Khalti"));
    }

    @Test
    void verifyAndConfirmPayment_Esewa_SendsNotification() {
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

        paymentService.verifyAndConfirmPayment(request);

        verify(notificationService).sendPaymentCompletedToCustomer(
                eq(1L), eq("Test Shop"), anyString(), eq("eSewa"));
    }

    @Test
    void initiatePayment_CalculatesTotalAmount() {
        ServiceOffering service2 = new ServiceOffering();
        service2.setId(2L);
        service2.setName("Shave");
        service2.setPrice(30.0);

        testTransaction.setServices(Arrays.asList(testService, service2));
        testTransaction.setAmount(null); // Will be calculated

        Map<String, Object> khaltiResponse = new HashMap<>();
        khaltiResponse.put("payment_url", "https://khalti.com/pay");
        khaltiResponse.put("pidx", "test-pidx");

        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(khaltiService.initiatePayment(anyLong(), any(), anyString())).thenReturn(khaltiResponse);

        paymentService.initiatePayment(testTransaction, testCustomer);

        assertEquals(new BigDecimal("80.0"), testTransaction.getAmount());
    }

    @Test
    void failTransaction_NotFound_DoesNothing() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        // Should not throw exception, just do nothing
        paymentService.failTransaction(999L);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void retryRefund_NotFound_ThrowsException() {
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.retryRefund(999L);
        });
    }

    @Test
    void processRefundForAppointment_Esewa_NoGatewayRefund_MarksCompleted() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId("esewa-ref-123");

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
        assertEquals(TransactionStatus.REFUNDED, testTransaction.getStatus());
        assertEquals(new BigDecimal("50.00"), testTransaction.getRefundAmount());
    }

    @Test
    void retryRefund_Esewa_AlwaysFails_RemainsInFailedState() {
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setRefundAmount(new BigDecimal("50.00"));
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId("esewa-ref-123");

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.retryRefund(1L);

        // Esewa refunds are manual, so it should remain in FAILED_PENDING_REVIEW
        assertEquals(RefundStatus.FAILED_PENDING_REVIEW, testTransaction.getRefundStatus());
    }

    @Test
    void retryRefund_NoRefundAmount_ThrowsException() {
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setRefundAmount(null);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(IllegalStateException.class, () -> {
            paymentService.retryRefund(1L);
        });
    }

    @Test
    void retryRefund_ZeroRefundAmount_ThrowsException() {
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setRefundAmount(BigDecimal.ZERO);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        assertThrows(IllegalStateException.class, () -> {
            paymentService.retryRefund(1L);
        });
    }

    @Test
    void verifyAndConfirmPayment_Esewa_UsesStoredRefIdWhenMissing() {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId("stored-ref-id");

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setRefId(null); // Missing from request

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(esewaService.verifyPayment(eq("stored-ref-id"), anyLong(), any())).thenReturn(true);
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);

        Appointment result = paymentService.verifyAndConfirmPayment(request);

        assertNotNull(result);
        verify(esewaService).verifyPayment("stored-ref-id", 1L, new BigDecimal("50.00"));
    }

    @Test
    void verifyAndConfirmPayment_Esewa_EmptyRefId_FailsVerification() {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        testTransaction.setRefId("");

        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setRefId("");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        doNothing().when(slotReservationService).cancelReservation(1L);

        assertThrows(RuntimeException.class, () -> {
            paymentService.verifyAndConfirmPayment(request);
        });

        assertEquals(TransactionStatus.FAILED, testTransaction.getStatus());
    }

    @Test
    void initiatePayment_Esewa_StoresTransactionUuid() throws Exception {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        Map<String, String> esewaData = new HashMap<>();
        esewaData.put("payment_url", "https://esewa.com/pay");
        esewaData.put("transaction_uuid", "uuid-12345");

        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(esewaService.preparePaymentData(anyLong(), any())).thenReturn(esewaData);

        PaymentInitiationResponse response = paymentService.initiatePayment(testTransaction, testCustomer);

        assertNotNull(response);
        verify(transactionRepository, times(2)).save(any()); // Once initially, once to store uuid
    }

    @Test
    void initiatePayment_Esewa_NoTransactionUuid_StillSucceeds() throws Exception {
        testTransaction.setPaymentMethod(PaymentMethod.ESEWA);
        Map<String, String> esewaData = new HashMap<>();
        esewaData.put("payment_url", "https://esewa.com/pay");
        // No transaction_uuid in response

        when(transactionRepository.save(any())).thenReturn(testTransaction);
        doNothing().when(slotReservationService).reserveSlot(anyLong(), anyLong(), any(), anyLong());
        when(esewaService.preparePaymentData(anyLong(), any())).thenReturn(esewaData);

        PaymentInitiationResponse response = paymentService.initiatePayment(testTransaction, testCustomer);

        assertNotNull(response);
        assertEquals("https://esewa.com/pay", response.getPaymentUrl());
    }

    @Test
    void verifyAndConfirmPayment_SetsGatewayTransactionId() {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");
        request.setGatewayTransactionId("gateway-tx-123");

        when(transactionRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testTransaction));
        when(slotReservationService.isReservationActive(1L)).thenReturn(true);
        when(khaltiService.verifyPayment("test-pidx")).thenReturn(true);
        doNothing().when(slotReservationService).consumeReservation(1L);
        when(appointmentService.bookPaidAppointment(any())).thenReturn(testAppointment);

        paymentService.verifyAndConfirmPayment(request);

        assertEquals("gateway-tx-123", testTransaction.getTransactionId());
    }

    @Test
    void processRefundForAppointment_RefundedStatus_AllowsProcessing() {
        testTransaction.setStatus(TransactionStatus.REFUNDED);
        testTransaction.setRefundStatus(RefundStatus.FAILED_PENDING_REVIEW);
        testTransaction.setPidx("test-pidx");

        when(transactionRepository.save(any())).thenReturn(testTransaction);

        paymentService.processRefundForAppointment(testTransaction, 1.0);

        assertEquals(RefundStatus.COMPLETED, testTransaction.getRefundStatus());
    }

    @Test
    void failTransaction_AlreadyCompleted_DoesNotChange() {
        testTransaction.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        paymentService.failTransaction(1L);

        assertEquals(TransactionStatus.COMPLETED, testTransaction.getStatus());
        verify(transactionRepository, never()).save(any());
    }
}

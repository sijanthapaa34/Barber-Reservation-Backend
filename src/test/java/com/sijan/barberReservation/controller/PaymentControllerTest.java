package com.sijan.barberReservation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sijan.barberReservation.DTO.appointment.AppointmentDetailsResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentInitiationResponse;
import com.sijan.barberReservation.DTO.appointment.PaymentRequestDTO;
import com.sijan.barberReservation.DTO.appointment.PaymentVerificationRequest;
import com.sijan.barberReservation.mapper.appointment.AppointmentDetailsMapper;
import com.sijan.barberReservation.mapper.appointment.PaymentMapper;
import com.sijan.barberReservation.model.*;
import com.sijan.barberReservation.repository.PaymentTransactionRepository;
import com.sijan.barberReservation.service.CustomerService;
import com.sijan.barberReservation.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private AppointmentDetailsMapper appointmentDetailsMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private PaymentMapper paymentMapper;

    @MockBean
    private PaymentTransactionRepository transactionRepository;

    private Customer testCustomer;
    private PaymentTransaction testTransaction;
    private Appointment testAppointment;
    private Barbershop testBarbershop;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@test.com");
        testCustomer.setPassword("password");
        testCustomer.setRole(Roles.CUSTOMER);
        testCustomer.setActive(true);

        testUserPrincipal = new UserPrincipal(testCustomer);

        testBarbershop = new Barbershop();
        testBarbershop.setId(1L);
        testBarbershop.setName("Test Shop");

        testAppointment = new Appointment();
        testAppointment.setId(1L);
        testAppointment.setCustomer(testCustomer);
        testAppointment.setBarbershop(testBarbershop);

        testTransaction = new PaymentTransaction();
        testTransaction.setId(1L);
        testTransaction.setAmount(new BigDecimal("50.00"));
        testTransaction.setStatus(TransactionStatus.COMPLETED);
        testTransaction.setPaymentMethod(PaymentMethod.KHALTI);
        testTransaction.setCustomer(testCustomer);
        testTransaction.setBarbershop(testBarbershop);
        testTransaction.setAppointment(testAppointment);
        testTransaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void initiatePayment_Success() throws Exception {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setBarberId(1L);
        request.setBarbershopId(1L);
        request.setServiceIds(Arrays.asList(1L));
        request.setScheduledTime(LocalDateTime.now().plusDays(1));
        request.setPaymentMethod(PaymentMethod.KHALTI);

        PaymentInitiationResponse response = new PaymentInitiationResponse(
                1L, "https://payment.url", "KHALTI", "test-pidx", new java.util.HashMap<>());

        when(customerService.findById(anyLong())).thenReturn(testCustomer);
        when(paymentMapper.toEntity(any())).thenReturn(testTransaction);
        when(paymentService.initiatePayment(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/payments/initiate")
                        .with(csrf())
                        .with(user(testUserPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.paymentUrl").value("https://payment.url"));

        verify(paymentService).initiatePayment(any(), any());
    }

    @Test
    @WithMockUser
    void verifyPayment_Success() throws Exception {
        PaymentVerificationRequest request = new PaymentVerificationRequest();
        request.setTransactionId(1L);
        request.setPidx("test-pidx");
        request.setRefId("test-ref");

        AppointmentDetailsResponse response = new AppointmentDetailsResponse();
        response.setAppointmentId(1L);

        when(paymentService.verifyAndConfirmPayment(any())).thenReturn(testAppointment);
        when(appointmentDetailsMapper.toDTO(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1));

        verify(paymentService).verifyAndConfirmPayment(any());
    }

    @Test
    @WithMockUser
    void cancelPayment_Success() throws Exception {
        doNothing().when(paymentService).cancelPayment(1L);

        mockMvc.perform(post("/api/payments/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(paymentService).cancelPayment(1L);
    }

    @Test
    @WithMockUser
    void retryRefund_Success() throws Exception {
        doNothing().when(paymentService).retryRefund(1L);

        mockMvc.perform(post("/api/payments/1/retry-refund")
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(paymentService).retryRefund(1L);
    }

    @Test
    void getPaymentHistory_Success() throws Exception {
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.totalSpent").exists())
                .andExpect(jsonPath("$.totalRefunded").exists())
                .andExpect(jsonPath("$.transactionCount").value(1));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithRefundedTransaction() throws Exception {
        testTransaction.setRefundStatus(RefundStatus.COMPLETED);
        testTransaction.setRefundAmount(new BigDecimal("25.00"));
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].refundStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.transactions[0].refundAmount").value(25.00));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_EmptyList() throws Exception {
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isEmpty())
                .andExpect(jsonPath("$.transactionCount").value(0));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithNullBarbershop() throws Exception {
        testTransaction.setBarbershop(null);
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].shopName").doesNotExist());

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithNullAppointment() throws Exception {
        testTransaction.setAppointment(null);
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].services").doesNotExist());

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithNullServices() throws Exception {
        testAppointment.setServices(null);
        testTransaction.setAppointment(testAppointment);
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].services").doesNotExist());

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithPendingTransaction_ShouldFilter() throws Exception {
        PaymentTransaction pendingTx = new PaymentTransaction();
        pendingTx.setId(2L);
        pendingTx.setAmount(new BigDecimal("100.00"));
        pendingTx.setStatus(TransactionStatus.PENDING);
        pendingTx.setPaymentMethod(PaymentMethod.KHALTI);
        pendingTx.setCustomer(testCustomer);

        List<PaymentTransaction> transactions = Arrays.asList(testTransaction, pendingTx);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].id").value(1));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithFailedTransaction_ShouldFilter() throws Exception {
        PaymentTransaction failedTx = new PaymentTransaction();
        failedTx.setId(3L);
        failedTx.setAmount(new BigDecimal("75.00"));
        failedTx.setStatus(TransactionStatus.FAILED);
        failedTx.setPaymentMethod(PaymentMethod.KHALTI);
        failedTx.setCustomer(testCustomer);

        List<PaymentTransaction> transactions = Arrays.asList(testTransaction, failedTx);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.totalSpent").value(50.00));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithRefundPending_ShouldNotCountInTotal() throws Exception {
        testTransaction.setRefundStatus(RefundStatus.PENDING);
        testTransaction.setRefundAmount(new BigDecimal("25.00"));
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRefunded").value(0));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithNullRefundAmount() throws Exception {
        testTransaction.setRefundStatus(RefundStatus.COMPLETED);
        testTransaction.setRefundAmount(null);
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRefunded").value(0));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }

    @Test
    void getPaymentHistory_WithMultipleServices() throws Exception {
        ServiceOffering service1 = new ServiceOffering();
        service1.setName("Haircut");
        ServiceOffering service2 = new ServiceOffering();
        service2.setName("Shave");
        
        testAppointment.setServices(Arrays.asList(service1, service2));
        testTransaction.setAppointment(testAppointment);
        List<PaymentTransaction> transactions = Arrays.asList(testTransaction);

        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(anyLong()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/payments/history")
                        .with(user(testUserPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].services").value("Haircut, Shave"));

        verify(transactionRepository).findByCustomerIdOrderByCreatedAtDesc(anyLong());
    }
}

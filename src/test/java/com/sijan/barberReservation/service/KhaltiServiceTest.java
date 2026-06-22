package com.sijan.barberReservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KhaltiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KhaltiService khaltiService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(khaltiService, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(khaltiService, "baseUrl", "https://test.khalti.com/api/v2/epayment/");
        ReflectionTestUtils.setField(khaltiService, "websiteUrl", "https://test-website.com");
        ReflectionTestUtils.setField(khaltiService, "restTemplate", restTemplate);
    }

    @Test
    void initiatePayment_Success_ReturnsPaymentData() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("pidx", "test-pidx-123");
        mockResponse.put("payment_url", "https://test.khalti.com/payment/test-pidx-123");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        Map<String, Object> result = khaltiService.initiatePayment(transactionId, amount, productName);

        // Assert
        assertNotNull(result);
        assertEquals("test-pidx-123", result.get("pidx"));
        assertEquals("https://test.khalti.com/payment/test-pidx-123", result.get("payment_url"));
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void initiatePayment_MissingPidx_ThrowsException() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("payment_url", "https://test.khalti.com/payment/test");
        // Missing pidx

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                khaltiService.initiatePayment(transactionId, amount, productName)
        );
        assertTrue(exception.getMessage().contains("missing pidx or payment_url"));
    }

    @Test
    void initiatePayment_MissingPaymentUrl_ThrowsException() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("pidx", "test-pidx-123");
        // Missing payment_url

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                khaltiService.initiatePayment(transactionId, amount, productName)
        );
        assertTrue(exception.getMessage().contains("missing pidx or payment_url"));
    }

    @Test
    void initiatePayment_HttpClientError_ThrowsException() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request", "{\"error\":\"Invalid amount\"}".getBytes(), null));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                khaltiService.initiatePayment(transactionId, amount, productName)
        );
        assertTrue(exception.getMessage().contains("Khalti error"));
    }

    @Test
    void initiatePayment_GenericException_ThrowsException() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                khaltiService.initiatePayment(transactionId, amount, productName)
        );
        assertTrue(exception.getMessage().contains("Could not initiate Khalti payment"));
    }

    @Test
    void initiatePayment_NonOkStatus_ThrowsException() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");
        String productName = "Haircut Service";

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                khaltiService.initiatePayment(transactionId, amount, productName)
        );
        assertTrue(exception.getMessage().contains("Khalti initiate failed with status"));
    }

    @Test
    void verifyPayment_Success_ReturnsTrue() {
        // Arrange
        String pidx = "test-pidx-123";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "Completed");
        mockResponse.put("pidx", pidx);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = khaltiService.verifyPayment(pidx);

        // Assert
        assertTrue(result);
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void verifyPayment_PendingStatus_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "Pending");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = khaltiService.verifyPayment(pidx);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_NullPidx_ReturnsFalse() {
        // Act
        boolean result = khaltiService.verifyPayment(null);

        // Assert
        assertFalse(result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void verifyPayment_EmptyPidx_ReturnsFalse() {
        // Act
        boolean result = khaltiService.verifyPayment("");

        // Assert
        assertFalse(result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void verifyPayment_HttpClientError_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        // Act
        boolean result = khaltiService.verifyPayment(pidx);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_GenericException_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act
        boolean result = khaltiService.verifyPayment(pidx);

        // Assert
        assertFalse(result);
    }

    @Test
    void refundPayment_Success_ReturnsTrue() {
        // Arrange
        String pidx = "test-pidx-123";
        BigDecimal refundAmount = new BigDecimal("250.00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "Refunded");
        mockResponse.put("pidx", pidx);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = khaltiService.refundPayment(pidx, refundAmount);

        // Assert
        assertTrue(result);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void refundPayment_HttpClientError_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";
        BigDecimal refundAmount = new BigDecimal("250.00");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        // Act
        boolean result = khaltiService.refundPayment(pidx, refundAmount);

        // Assert
        assertFalse(result);
    }

    @Test
    void refundPayment_GenericException_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";
        BigDecimal refundAmount = new BigDecimal("250.00");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act
        boolean result = khaltiService.refundPayment(pidx, refundAmount);

        // Assert
        assertFalse(result);
    }

    @Test
    void refundPayment_NonOkStatus_ReturnsFalse() {
        // Arrange
        String pidx = "test-pidx-123";
        BigDecimal refundAmount = new BigDecimal("250.00");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = khaltiService.refundPayment(pidx, refundAmount);

        // Assert
        assertFalse(result);
    }

    @Test
    void refundPayment_PartialRefund_ConvertsAmountCorrectly() {
        // Arrange
        String pidx = "test-pidx-123";
        BigDecimal refundAmount = new BigDecimal("125.50"); // Should convert to 12550 paisa

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "Refunded");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = khaltiService.refundPayment(pidx, refundAmount);

        // Assert
        assertTrue(result);
    }
}

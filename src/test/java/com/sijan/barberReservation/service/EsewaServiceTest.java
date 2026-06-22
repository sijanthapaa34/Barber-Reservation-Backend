package com.sijan.barberReservation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EsewaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EsewaService esewaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(esewaService, "merchantId", "EPAYTEST");
        ReflectionTestUtils.setField(esewaService, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(esewaService, "baseUrl", "https://rc.esewa.com.np/api/epay/");
        ReflectionTestUtils.setField(esewaService, "successUrl", "https://test.com/success");
        ReflectionTestUtils.setField(esewaService, "failureUrl", "https://test.com/failure");
        ReflectionTestUtils.setField(esewaService, "restTemplate", restTemplate);
    }

    @Test
    void preparePaymentData_Success_ReturnsPaymentData() {
        // Arrange
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        // Act
        Map<String, String> result = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        assertNotNull(result);
        assertEquals("500.00", result.get("total_amount"));
        assertEquals("500.00", result.get("amount"));
        assertEquals("0", result.get("tax_amount"));
        assertEquals("0", result.get("product_service_charge"));
        assertEquals("0", result.get("product_delivery_charge"));
        assertEquals("EPAYTEST", result.get("product_code"));
        assertTrue(result.get("transaction_uuid").startsWith("123-"));
        assertNotNull(result.get("signature"));
        assertNotNull(result.get("payment_url"));
        assertTrue(result.get("success_url").contains("txId=123"));
        assertTrue(result.get("failure_url").contains("txId=123"));
    }

    @Test
    void preparePaymentData_GeneratesUniqueTransactionUuid() {
        // Arrange
        Long transactionId = 456L;
        BigDecimal amount = new BigDecimal("1000.00");

        // Act
        Map<String, String> result1 = esewaService.preparePaymentData(transactionId, amount);
        
        // Wait a bit to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, String> result2 = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        assertNotEquals(result1.get("transaction_uuid"), result2.get("transaction_uuid"));
    }

    @Test
    void preparePaymentData_RoundsAmountCorrectly() {
        // Arrange
        Long transactionId = 789L;
        BigDecimal amount = new BigDecimal("123.456");

        // Act
        Map<String, String> result = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        assertEquals("123.46", result.get("total_amount"));
        assertEquals("123.46", result.get("amount"));
    }

    @Test
    void preparePaymentData_IncludesAllRequiredFields() {
        // Arrange
        Long transactionId = 100L;
        BigDecimal amount = new BigDecimal("250.00");

        // Act
        Map<String, String> result = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        assertTrue(result.containsKey("payment_url"));
        assertTrue(result.containsKey("amount"));
        assertTrue(result.containsKey("tax_amount"));
        assertTrue(result.containsKey("product_service_charge"));
        assertTrue(result.containsKey("product_delivery_charge"));
        assertTrue(result.containsKey("total_amount"));
        assertTrue(result.containsKey("transaction_uuid"));
        assertTrue(result.containsKey("product_code"));
        assertTrue(result.containsKey("success_url"));
        assertTrue(result.containsKey("failure_url"));
        assertTrue(result.containsKey("signed_field_names"));
        assertTrue(result.containsKey("signature"));
    }

    @Test
    void verifyPayment_SandboxMode_ReturnsTrue() {
        // Arrange
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertTrue(result);
        verifyNoInteractions(restTemplate); // Should not call API in sandbox mode
    }

    @Test
    void verifyPayment_ProductionMode_CompletedStatus_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "COMPLETE");
        mockResponse.put("transaction_uuid", refId);

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertTrue(result);
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

    @Test
    void verifyPayment_ProductionMode_CompletedStatusLowercase_ReturnsTrue() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "COMPLETED");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertTrue(result);
    }

    @Test
    void verifyPayment_ProductionMode_PendingStatus_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "PENDING");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_ProductionMode_FailedStatus_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("status", "FAILED");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_ProductionMode_Exception_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_ProductionMode_NonOkStatus_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertFalse(result);
    }

    @Test
    void verifyPayment_ProductionMode_NullResponseBody_ReturnsFalse() {
        // Arrange
        ReflectionTestUtils.setField(esewaService, "merchantId", "PROD-MERCHANT-ID");
        String refId = "123-1234567890";
        Long transactionId = 123L;
        BigDecimal amount = new BigDecimal("500.00");

        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Act
        boolean result = esewaService.verifyPayment(refId, transactionId, amount);

        // Assert
        assertFalse(result);
    }

    @Test
    void preparePaymentData_GeneratesValidSignature() {
        // Arrange
        Long transactionId = 999L;
        BigDecimal amount = new BigDecimal("750.00");

        // Act
        Map<String, String> result = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        String signature = result.get("signature");
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        // Signature should be base64 encoded
        assertTrue(signature.matches("^[A-Za-z0-9+/=]+$"));
    }

    @Test
    void preparePaymentData_IncludesCorrectSignedFieldNames() {
        // Arrange
        Long transactionId = 111L;
        BigDecimal amount = new BigDecimal("100.00");

        // Act
        Map<String, String> result = esewaService.preparePaymentData(transactionId, amount);

        // Assert
        assertEquals("total_amount,transaction_uuid,product_code", result.get("signed_field_names"));
    }
}

package com.sijan.barberReservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EsewaService {

    @Value("${esewa.merchant-id}")
    private String merchantId;

    @Value("${esewa.secret-key}")
    private String secretKey;

    @Value("${esewa.base-url}")
    private String baseUrl; // Should be: https://rc.esewa.com.np/api/epay/

    @Value("${esewa.success-url}")
    private String successUrl;

    @Value("${esewa.failure-url}")
    private String failureUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private String generateSignature(String message) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hash = sha256_HMAC.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating eSewa signature", e);
        }
    }

    public Map<String, String> preparePaymentData(Long transactionId, BigDecimal amount) {
        String totalAmountStr = amount.setScale(2, RoundingMode.HALF_UP).toString();

        // ✅ FIX: Create unique transaction UUID by appending timestamp
        // This prevents "Duplicate transaction UUID" errors on retries
        String uniqueTransactionUuid = transactionId.toString() + "-" + System.currentTimeMillis();

        // eSewa V2 Signature format
        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        String messageToSign = String.format(
                "total_amount=%s,transaction_uuid=%s,product_code=%s",
                totalAmountStr,
                uniqueTransactionUuid,
                merchantId
        );

        String signature = generateSignature(messageToSign);

        Map<String, String> data = new HashMap<>();

        // ✅ FIXED: Correct payment URL for eSewa V2 form submission
        data.put("payment_url", baseUrl + "main/v2/form");

        // Form Data
        data.put("amount", totalAmountStr);
        data.put("tax_amount", "0");
        data.put("product_service_charge", "0");
        data.put("product_delivery_charge", "0");
        data.put("total_amount", totalAmountStr);
        data.put("transaction_uuid", uniqueTransactionUuid);
        data.put("product_code", merchantId);
        data.put("success_url", successUrl + "?txId=" + transactionId + "&refId=" + uniqueTransactionUuid);
        data.put("failure_url", failureUrl + "?txId=" + transactionId + "&status=failure");
        data.put("signed_field_names", signedFieldNames);
        data.put("signature", signature);

        log.info("eSewa Payment URL: {}", data.get("payment_url"));
        log.info("eSewa Transaction UUID: {}", uniqueTransactionUuid);
        log.info("eSewa Success URL: {}", data.get("success_url"));
        log.info("eSewa Failure URL: {}", data.get("failure_url"));
        log.info("eSewa Signature: {}", signature);

        return data;
    }

    public boolean verifyPayment(String refId, Long transactionId, BigDecimal amount) {
        // ✅ FIX: Extract the unique UUID from refId (format: "txId-timestamp")
        // The refId passed here is the uniqueTransactionUuid from preparePaymentData
        String transactionUuid = refId;
        
        // ✅ FIXED: Correct verification endpoint for eSewa V2
        String url = String.format(
                "%smain/v2/form/transaction/status/?product_code=%s&total_amount=%s&transaction_uuid=%s",
                baseUrl,
                merchantId,
                amount.setScale(2, RoundingMode.HALF_UP).toString(),
                transactionUuid
        );

        log.info("eSewa Verify URL: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> resBody = response.getBody();
                String status = (String) resBody.get("status");
                log.info("eSewa Verify Response: {}", resBody);

                // eSewa V2 returns "COMPLETE" for successful payments
                return "COMPLETE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
            }
        } catch (Exception e) {
            log.error("eSewa verify error: {}", e.getMessage(), e);
        }
        return false;
    }
}
package com.sijan.barberReservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KhaltiService {

    @Value("${khalti.secret-key}")
    private String secretKey;

    @Value("${khalti.base-url}")
    private String baseUrl;

    @Value("${khalti.website-url}")
    private String websiteUrl;

    private final RestTemplate restTemplate;

    public KhaltiService() {
        // ✅ This tells Java's HTTP client to KEEP the POST method on 302 redirects
        // (By default, Java changes POST to GET, which causes Khalti's 404 error)
        System.setProperty("http.strictPostRedirect", "true");
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Object> initiatePayment(Long transactionId, BigDecimal amount, String productName) {
        String url = baseUrl + "initiate/";
        log.info("Khalti Initiate URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + secretKey);

        Map<String, Object> body = new HashMap<>();

        // !!!!! LOOK HERE !!!!!
        body.put("return_url", "https://www.google.com");

        body.put("website_url", websiteUrl);
        body.put("amount", amount.multiply(new BigDecimal("100")).longValue());
        body.put("purchase_order_id", String.valueOf(transactionId));
        body.put("purchase_order_name", productName);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("Khalti Initiate Response: {}", responseBody);

                if (responseBody.get("pidx") == null || responseBody.get("payment_url") == null) {
                    log.error("Khalti response missing required fields: {}", responseBody);
                    throw new RuntimeException("Invalid response from Khalti: missing pidx or payment_url");
                }

                return responseBody;
            } else {
                log.error("Khalti initiate failed with status: {}", response.getStatusCode());
                throw new RuntimeException("Khalti initiate failed with status: " + response.getStatusCode());
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Khalti initiate error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Khalti error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Khalti initiate error: {}", e.getMessage(), e);
            throw new RuntimeException("Could not initiate Khalti payment: " + e.getMessage());
        }
    }

    public boolean verifyPayment(String pidx) {
        if (pidx == null || pidx.isEmpty()) {
            log.error("Khalti verify called with null/empty pidx");
            return false;
        }

        String url = baseUrl + "lookup/";
        log.info("Khalti Lookup URL: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + secretKey);

        Map<String, String> body = new HashMap<>();
        body.put("pidx", pidx);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> resBody = response.getBody();
                String status = (String) resBody.get("status");
                log.info("Khalti Lookup Response: {}", resBody);

                // Khalti V2 returns "Completed" for successful payments
                return "Completed".equalsIgnoreCase(status);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Khalti lookup error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Khalti lookup error: {}", e.getMessage(), e);
        }
        return false;
    }
    /**
     * Refund payment to Khalti (Partial or Full)
     * Amount should be in Rupees (e.g., 250.00), method converts to Paisa
     * @return true if refund successful, false otherwise
     */
    public boolean refundPayment(String pidx, BigDecimal refundAmountInRupees) {
        long amountInPaisa = refundAmountInRupees.multiply(new BigDecimal("100")).longValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + secretKey);

        Map<String, Object> body = new HashMap<>();
        body.put("pidx", pidx);
        body.put("amount", amountInPaisa);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Khalti refund API only exists on production endpoint regardless of environment
        String refundUrl = "https://a.khalti.com/api/v2/epayment/refund/";

        log.info("Initiating Khalti Refund: url={}, pidx={}, amount={} paisa", refundUrl, pidx, amountInPaisa);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    refundUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            log.info("Khalti Refund Response: {}", responseBody);

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                // Check Khalti's refund response for success indicator
                // Khalti typically returns the refund transaction details on success
                String status = (String) responseBody.get("status");
                // You might also check for "Refunded" or other indicators based on Khalti docs
                return response.getStatusCode().is2xxSuccessful();
            }
            return false;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Khalti refund error ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (Exception e) {
            log.error("Khalti refund exception for pidx={}: {}", pidx, e.getMessage(), e);
            return false;
        }
    }
}
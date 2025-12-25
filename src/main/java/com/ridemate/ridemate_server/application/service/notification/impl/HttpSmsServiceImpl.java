package com.ridemate.ridemate_server.application.service.notification.impl;

import com.ridemate.ridemate_server.application.service.notification.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP-based SMS service implementation for Vietnamese SMS providers
 * Supports providers like BrandSMS, ESMS, etc.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "http", matchIfMissing = false)
public class HttpSmsServiceImpl implements SmsService {

    @Value("${sms.http.api-url:}")
    private String apiUrl;

    @Value("${sms.http.api-key:}")
    private String apiKey;

    @Value("${sms.http.api-secret:}")
    private String apiSecret;

    @Value("${sms.http.brand-name:}")
    private String brandName;

    @Value("${sms.http.template-id:}")
    private String templateId;

    private final RestTemplate restTemplate = new RestTemplate();
    private boolean enabled = false;

    @PostConstruct
    public void init() {
        if (apiUrl != null && !apiUrl.isEmpty() && 
            apiKey != null && !apiKey.isEmpty()) {
            enabled = true;
            log.info("✅ HTTP SMS service initialized. API URL: {}", apiUrl);
        } else {
            log.warn("⚠️ HTTP SMS credentials not configured. SMS will not be sent.");
            log.warn("   Set SMS_HTTP_API_URL and SMS_HTTP_API_KEY in .env");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) throws Exception {
        if (!enabled) {
            log.warn("HTTP SMS service is not enabled. Message would be: {} -> {}", phoneNumber, message);
            return false;
        }

        try {
            // Normalize phone number
            String normalizedPhone = normalizePhoneNumber(phoneNumber);
            
            log.info("📱 Sending SMS via HTTP to {}: {}", normalizedPhone, message.substring(0, Math.min(50, message.length())));
            
            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phone", normalizedPhone);
            requestBody.put("message", message);
            
            if (brandName != null && !brandName.isEmpty()) {
                requestBody.put("brandName", brandName);
            }
            
            if (templateId != null && !templateId.isEmpty()) {
                requestBody.put("templateId", templateId);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ SMS sent successfully via HTTP");
                return true;
            } else {
                log.error("❌ Failed to send SMS via HTTP. Status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Failed to send SMS via HTTP: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Normalize Vietnamese phone number
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all spaces and dashes
        String cleaned = phoneNumber.replaceAll("[\\s\\-]", "");

        // Remove +84 prefix if present (some APIs expect local format)
        if (cleaned.startsWith("+84")) {
            return "0" + cleaned.substring(3);
        }

        // If starts with 84 (without +), convert to 0
        if (cleaned.startsWith("84") && cleaned.length() == 11) {
            return "0" + cleaned.substring(2);
        }

        // Return as is if already in local format (0xxxxxxxxx)
        return cleaned;
    }
}


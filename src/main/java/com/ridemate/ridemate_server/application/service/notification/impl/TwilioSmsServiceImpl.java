package com.ridemate.ridemate_server.application.service.notification.impl;

import com.ridemate.ridemate_server.application.service.notification.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Slf4j
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio", matchIfMissing = false)
public class TwilioSmsServiceImpl implements SmsService {

    @Value("${sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${sms.twilio.auth-token:}")
    private String authToken;

    @Value("${sms.twilio.from-number:}")
    private String fromNumber;

    private boolean enabled = false;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isEmpty() && 
            authToken != null && !authToken.isEmpty() &&
            fromNumber != null && !fromNumber.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                enabled = true;
                log.info("✅ Twilio SMS service initialized successfully");
                log.info("   Account SID: {}...{}", accountSid.substring(0, Math.min(4, accountSid.length())), 
                        accountSid.length() > 4 ? accountSid.substring(accountSid.length() - 4) : "");
                log.info("   From Number: {}", fromNumber);
            } catch (Exception e) {
                log.error("❌ Failed to initialize Twilio SMS service: {}", e.getMessage());
                log.error("   Error details: ", e);
                enabled = false;
            }
        } else {
            log.warn("⚠️ Twilio SMS credentials not configured. SMS will not be sent.");
            log.warn("   Account SID: {}", accountSid != null && !accountSid.isEmpty() ? "✅ Set" : "❌ Missing");
            log.warn("   Auth Token: {}", authToken != null && !authToken.isEmpty() ? "✅ Set" : "❌ Missing");
            log.warn("   From Number: {}", fromNumber != null && !fromNumber.isEmpty() ? "✅ Set" : "❌ Missing");
            log.warn("   Set SMS_TWILIO_ACCOUNT_SID, SMS_TWILIO_AUTH_TOKEN, and SMS_TWILIO_FROM_NUMBER in .env");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) throws Exception {
        if (!enabled) {
            log.warn("Twilio SMS service is not enabled. Message would be: {} -> {}", phoneNumber, message);
            return false;
        }

        try {
            // Normalize phone number to E.164 format
            String normalizedPhone = normalizePhoneNumber(phoneNumber);
            
            log.info("📱 Sending SMS via Twilio to {}: {}", normalizedPhone, message.substring(0, Math.min(50, message.length())));
            
            Message twilioMessage = Message.creator(
                    new PhoneNumber(normalizedPhone),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("✅ SMS sent successfully. SID: {}", twilioMessage.getSid());
            return true;
        } catch (Exception e) {
            log.error("❌ Failed to send SMS via Twilio: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Normalize Vietnamese phone number to E.164 format
     * Examples:
     * - 0901234567 -> +84901234567
     * - 0912345678 -> +84912345678
     * - +84901234567 -> +84901234567 (already in E.164)
     */
    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }

        // Remove all spaces and dashes
        String cleaned = phoneNumber.replaceAll("[\\s\\-]", "");

        // If already in E.164 format, return as is
        if (cleaned.startsWith("+84")) {
            return cleaned;
        }

        // If starts with 0, replace with +84
        if (cleaned.startsWith("0")) {
            return "+84" + cleaned.substring(1);
        }

        // If starts with 84 (without +), add +
        if (cleaned.startsWith("84")) {
            return "+" + cleaned;
        }

        // Default: assume it's a Vietnamese number starting with 0
        if (cleaned.length() == 10 && cleaned.startsWith("0")) {
            return "+84" + cleaned.substring(1);
        }

        // If none of the above, try to add +84 prefix
        return "+84" + cleaned;
    }
}


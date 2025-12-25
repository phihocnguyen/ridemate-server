package com.ridemate.ridemate_server.application.service.notification.impl;

import com.ridemate.ridemate_server.application.service.notification.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Sandbox/Mock SMS service for development and testing
 * Only logs the SMS message without actually sending it
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "sms.provider", havingValue = "sandbox", matchIfMissing = true)
public class SandboxSmsServiceImpl implements SmsService {

    private static final String SANDBOX_PREFIX = "[RIDEMATE-SANDBOX]";

    @Override
    public boolean sendSms(String phoneNumber, String message) throws Exception {
        String formattedMessage = String.format(
            "\n" +
            "========== SMS SANDBOX ==========\n" +
            "To: %s\n" +
            "Body: %s\n" +
            "=================================\n",
            phoneNumber, message
        );
        
        log.info("{} {}", SANDBOX_PREFIX, formattedMessage);
        return true;
    }

    @Override
    public boolean isEnabled() {
        return false; // Sandbox mode is not "enabled" (not sending real SMS)
    }
}


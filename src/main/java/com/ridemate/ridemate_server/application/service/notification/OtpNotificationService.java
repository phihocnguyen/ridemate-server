package com.ridemate.ridemate_server.application.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OtpNotificationService {

    private static final String OTP_SANDBOX_PREFIX = "[RIDEMATE-SANDBOX]";

    public void sendOtpViaSms(String phoneNumber, String otpCode, String purpose) {
        log.info("{} Sending OTP via SMS to {}: {}", OTP_SANDBOX_PREFIX, phoneNumber, otpCode);
        log.info("Purpose: {}", purpose);
        log.info("Valid for 5 minutes");
        
        simulateSmsDelivery(phoneNumber, otpCode);
    }

    public void sendOtpViaEmail(String email, String otpCode, String purpose) {
        log.info("{} Sending OTP via Email to {}: {}", OTP_SANDBOX_PREFIX, email, otpCode);
        log.info("Purpose: {}", purpose);
        log.info("Valid for 5 minutes");
        
        simulateEmailDelivery(email, otpCode);
    }

    private void simulateSmsDelivery(String phoneNumber, String otpCode) {
        String message = String.format(
            "SMS Message:\n" +
            "To: %s\n" +
            "Body: Your RideMate OTP is: %s. This code is valid for 5 minutes. Do not share with anyone.",
            phoneNumber, otpCode
        );
        log.info("\n========== SMS SANDBOX ==========\n{}\n=================================\n", message);
    }

    private void simulateEmailDelivery(String email, String otpCode) {
        String message = String.format(
            "Email:\n" +
            "To: %s\n" +
            "Subject: RideMate OTP Verification\n" +
            "Body: Your verification code is: %s\n" +
            "This code is valid for 5 minutes. Do not share with anyone.",
            email, otpCode
        );
        log.info("\n========== EMAIL SANDBOX ==========\n{}\n==================================\n", message);
    }
}

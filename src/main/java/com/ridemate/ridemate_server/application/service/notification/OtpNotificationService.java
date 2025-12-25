package com.ridemate.ridemate_server.application.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OtpNotificationService {

    private final SmsService smsService;

    @Autowired
    public OtpNotificationService(List<SmsService> smsServices) {
        // Spring will inject all SmsService implementations
        // We want the one that is enabled (not sandbox), or fallback to sandbox
        this.smsService = smsServices.stream()
                .filter(SmsService::isEnabled)
                .findFirst()
                .orElseGet(() -> smsServices.stream()
                        .filter(s -> s.getClass().getSimpleName().equals("SandboxSmsServiceImpl"))
                        .findFirst()
                        .orElse(smsServices.isEmpty() ? createFallbackSmsService() : smsServices.get(0))); // Fallback to first available
    }

    private SmsService createFallbackSmsService() {
        return new com.ridemate.ridemate_server.application.service.notification.impl.SandboxSmsServiceImpl();
    }

    public void sendOtpViaSms(String phoneNumber, String otpCode, String purpose) {
        if (smsService == null) {
            log.error("❌ SMS service is not available. Cannot send OTP to {}", phoneNumber);
            return;
        }

        String message = String.format(
            "Ma xac thuc RideMate cua ban la: %s. Ma co hieu luc trong 5 phut. Khong chia se ma nay voi ai.",
            otpCode
        );

        try {
            boolean sent = smsService.sendSms(phoneNumber, message);
            if (sent && smsService.isEnabled()) {
                log.info("✅ OTP SMS sent successfully to {} for purpose: {}", phoneNumber, purpose);
            } else {
                log.info("📝 OTP SMS logged (sandbox mode) to {} for purpose: {}", phoneNumber, purpose);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send OTP SMS to {}: {}", phoneNumber, e.getMessage(), e);
            // Don't throw exception - log it and continue
            // The OTP is still saved in database, user can request a new one
        }
    }

    public void sendOtpViaEmail(String email, String otpCode, String purpose) {
        // TODO: Implement email sending service
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

    /**
     * Check if SMS service is enabled (not sandbox mode)
     * @return true if real SMS service is enabled, false if sandbox mode
     */
    public boolean isSmsServiceEnabled() {
        return smsService != null && smsService.isEnabled();
    }
}

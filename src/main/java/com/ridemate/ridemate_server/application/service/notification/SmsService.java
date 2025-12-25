package com.ridemate.ridemate_server.application.service.notification;

/**
 * Service interface for sending SMS messages
 */
public interface SmsService {
    
    /**
     * Send SMS message to a phone number
     * @param phoneNumber Phone number in E.164 format (e.g., +84901234567) or local format (e.g., 0901234567)
     * @param message Message content to send
     * @return true if sent successfully, false otherwise
     * @throws Exception if sending fails
     */
    boolean sendSms(String phoneNumber, String message) throws Exception;
    
    /**
     * Check if SMS service is enabled and configured
     * @return true if service is available, false if in sandbox/mock mode
     */
    boolean isEnabled();
}


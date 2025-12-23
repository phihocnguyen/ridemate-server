package com.ridemate.ridemate_server.application.scheduler;

import com.ridemate.ridemate_server.domain.repository.TemporaryVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class TemporaryVerificationCleanupScheduler {
    
    @Autowired
    private TemporaryVerificationRepository tempVerificationRepository;
    
    /**
     * Clean up expired temporary verifications every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 ms
    public void cleanupExpiredVerifications() {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.info("Starting cleanup of expired temporary verifications...");
            
            tempVerificationRepository.deleteExpiredVerifications(now);
            
            log.info("Cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during temporary verification cleanup: {}", e.getMessage(), e);
        }
    }
}

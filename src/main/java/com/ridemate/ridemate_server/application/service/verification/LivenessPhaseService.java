package com.ridemate.ridemate_server.application.service.verification;

import com.ridemate.ridemate_server.application.dto.verification.PhaseVerificationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface LivenessPhaseService {
    /**
     * Verify a specific liveness phase
     * 
     * @param phoneNumber User's phone number
     * @param phase Phase name: LOOK_STRAIGHT, BLINK, TURN_LEFT
     * @param image Image captured during this phase
     * @return PhaseVerificationResponse with verification result
     */
    PhaseVerificationResponse verifyPhase(String phoneNumber, String phase, MultipartFile image) throws Exception;
    
    /**
     * Get current phase status for a user
     * 
     * @param phoneNumber User's phone number
     * @return Current phase information
     */
    PhaseVerificationResponse getCurrentPhaseStatus(String phoneNumber);
    
    /**
     * Reset liveness check for a user (for retry)
     * 
     * @param phoneNumber User's phone number
     */
    void resetLivenessCheck(String phoneNumber);
}

package com.ridemate.ridemate_server.application.service.verification;

import com.ridemate.ridemate_server.application.dto.verification.VerificationResponse;
import com.ridemate.ridemate_server.application.dto.verification.VerificationStatusResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VerificationService {
    
    /**
     * Process ID card upload and store for verification
     * @param phoneNumber User's phone number
     * @param idCardImage ID card image file
     * @return Verification response with status
     */
    VerificationResponse verifyIdCard(String phoneNumber, MultipartFile idCardImage) throws Exception;
    
    /**
     * Process liveness check selfie and compare with ID card
     * @param phoneNumber User's phone number
     * @param selfieImage Selfie image from liveness check
     * @return Verification response with similarity score
     */
    VerificationResponse verifyLiveness(String phoneNumber, MultipartFile selfieImage) throws Exception;
    
    /**
     * Get verification status for a user
     * @param phoneNumber User's phone number
     * @return Verification status details
     */
    VerificationStatusResponse getVerificationStatus(String phoneNumber);
    
    /**
     * Process ID card upload with temporary identifier (for registration flow)
     * @param tempId Temporary identifier
     * @param idCardImage ID card image file
     * @return Verification response with status
     */
    VerificationResponse verifyIdCardWithTempId(String tempId, MultipartFile idCardImage) throws Exception;
    
    /**
     * Process liveness check and compare with ID card using temporary identifier
     * @param tempId Temporary identifier
     * @param selfieImage Selfie image from liveness check
     * @return Verification response with similarity score
     */
    VerificationResponse verifyLivenessWithTempId(String tempId, MultipartFile selfieImage) throws Exception;
    
    /**
     * Link temporary verification data to user account
     * @param tempId Temporary identifier
     * @param phoneNumber User's phone number
     * @throws Exception if temp verification not found or already used
     */
    void linkTempVerificationToUser(String tempId, String phoneNumber) throws Exception;
}

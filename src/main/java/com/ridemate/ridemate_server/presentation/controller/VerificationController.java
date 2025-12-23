package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.verification.VerificationResponse;
import com.ridemate.ridemate_server.application.dto.verification.VerificationStatusResponse;
import com.ridemate.ridemate_server.application.dto.verification.PhaseVerificationResponse;
import com.ridemate.ridemate_server.application.service.verification.VerificationService;
import com.ridemate.ridemate_server.application.service.verification.LivenessPhaseService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/verification")
@Tag(name = "Verification", description = "ID card and liveness verification endpoints")
@Slf4j
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping(value = "/id-card", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ID card for verification", description = "Upload Vietnamese citizen ID card (CCCD) image")
    @SecurityRequirements()
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "ID card uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VerificationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid image or no face detected"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<VerificationResponse>> uploadIdCard(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("idCardImage") MultipartFile idCardImage) {
        
        try {
            log.info("Received ID card upload request for phone: {}", phoneNumber);
            
            if (idCardImage == null || idCardImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "ID card image is required"));
            }

            VerificationResponse response = verificationService.verifyIdCard(phoneNumber, idCardImage);
            return ResponseEntity.ok(ApiResponse.success("ID card processed successfully", response));
            
        } catch (Exception e) {
            log.error("Error processing ID card upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to process ID card: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/liveness", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify liveness with selfie", description = "Upload selfie from liveness check and compare with ID card")
    @SecurityRequirements()
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liveness verification completed",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VerificationResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid image or verification failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyLiveness(
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("selfieImage") MultipartFile selfieImage) {
        
        try {
            log.info("Received liveness verification request for phone: {}", phoneNumber);
            
            if (selfieImage == null || selfieImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Selfie image is required"));
            }

            VerificationResponse response = verificationService.verifyLiveness(phoneNumber, selfieImage);
            return ResponseEntity.ok(ApiResponse.success("Liveness verification completed", response));
            
        } catch (Exception e) {
            log.error("Error processing liveness verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to verify liveness: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{phoneNumber}")
    @Operation(summary = "Get verification status", description = "Check verification status for a phone number")
    @SecurityRequirements()
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VerificationStatusResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> getVerificationStatus(
            @PathVariable String phoneNumber) {
        
        try {
            log.info("Getting verification status for phone: {}", phoneNumber);
            VerificationStatusResponse response = verificationService.getVerificationStatus(phoneNumber);
            return ResponseEntity.ok(ApiResponse.success("Verification status retrieved", response));
            
        } catch (Exception e) {
            log.error("Error getting verification status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to get verification status: " + e.getMessage()));
        }
    }

    @Autowired
    private LivenessPhaseService livenessPhaseService;

    @PostMapping(value = "/liveness/verify-phase", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify liveness phase")
    @SecurityRequirements()
    public ResponseEntity<ApiResponse<PhaseVerificationResponse>> verifyLivenessPhase(
            @RequestParam("phoneNumber") String identifier,
            @RequestParam("phase") String phase,
            @RequestParam("image") MultipartFile image) {
        
        try {
            log.info("Verifying phase {} for identifier: {}", phase, identifier);
            
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Image is required"));
            }

            PhaseVerificationResponse response = livenessPhaseService.verifyPhase(identifier, phase, image);
            return ResponseEntity.ok(ApiResponse.success("Phase verification completed", response));
            
        } catch (Exception e) {
            log.error("Error verifying phase: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to verify phase: " + e.getMessage()));
        }
    }
    
    @PostMapping(value = "/id-card-temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ID card with temporary identifier", 
               description = "Upload ID card for verification during registration (before phone number)")
    @SecurityRequirements()
    public ResponseEntity<ApiResponse<VerificationResponse>> uploadIdCardWithTempId(
            @RequestParam("tempId") String tempId,
            @RequestParam("idCardImage") MultipartFile idCardImage) {
        
        try {
            log.info("Received ID card upload request with tempId: {}", tempId);
            
            if (idCardImage == null || idCardImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "ID card image is required"));
            }

            VerificationResponse response = verificationService.verifyIdCardWithTempId(tempId, idCardImage);
            return ResponseEntity.ok(ApiResponse.success("ID card processed successfully", response));
            
        } catch (Exception e) {
            log.error("Error processing ID card upload with tempId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to process ID card: " + e.getMessage()));
        }
    }
    
    @PostMapping(value = "/liveness-temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Verify liveness with temporary identifier",
               description = "Compare liveness selfie with ID card using temporary identifier")
    @SecurityRequirements()
    public ResponseEntity<ApiResponse<VerificationResponse>> verifyLivenessWithTempId(
            @RequestParam("tempId") String tempId,
            @RequestParam("selfieImage") MultipartFile selfieImage) {
        
        try {
            log.info("Received liveness verification request with tempId: {}", tempId);
            
            if (selfieImage == null || selfieImage.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Selfie image is required"));
            }

            VerificationResponse response = verificationService.verifyLivenessWithTempId(tempId, selfieImage);
            return ResponseEntity.ok(ApiResponse.success("Liveness verification completed", response));
            
        } catch (Exception e) {
            log.error("Error verifying liveness with tempId: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to verify liveness: " + e.getMessage()));
        }
    }
    
    @PostMapping("/link-temp")
    @Operation(summary = "Link temporary verification to user account",
               description = "Transfer verification data from temporary storage to user account")
    @SecurityRequirements()
    public ResponseEntity<ApiResponse<String>> linkTempVerificationToUser(
            @RequestParam("tempId") String tempId,
            @RequestParam("phoneNumber") String phoneNumber) {
        
        try {
            log.info("Linking tempId {} to phoneNumber {}", tempId, phoneNumber);
            
            verificationService.linkTempVerificationToUser(tempId, phoneNumber);
            return ResponseEntity.ok(ApiResponse.success(
                    "Verification data linked successfully", 
                    "Temporary verification linked to user account"));
            
        } catch (Exception e) {
            log.error("Error linking temp verification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Failed to link verification: " + e.getMessage()));
        }
    }
}

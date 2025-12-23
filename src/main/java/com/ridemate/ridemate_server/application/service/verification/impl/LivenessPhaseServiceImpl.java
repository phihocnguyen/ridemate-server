package com.ridemate.ridemate_server.application.service.verification.impl;

import com.ridemate.ridemate_server.application.dto.verification.PhaseVerificationResponse;
import com.ridemate.ridemate_server.application.service.verification.LivenessPhaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class LivenessPhaseServiceImpl implements LivenessPhaseService {

    private final RestTemplate restTemplate;
    
    @Value("${face.service.url}")
    private String faceServiceUrl;
    
    // In-memory storage for phase results (use Redis in production)
    private final Map<String, PhaseData> phaseStorage = new ConcurrentHashMap<>();
    
    private static final String[] PHASE_ORDER = {"LOOK_STRAIGHT", "BLINK", "TURN_LEFT"};
    
    @Override
    public PhaseVerificationResponse verifyPhase(String phoneNumber, String phase, MultipartFile image) throws Exception {
        log.info("Verifying phase {} for phone: {}", phase, phoneNumber);
        
        // Validate phase
        if (!isValidPhase(phase)) {
            throw new IllegalArgumentException("Invalid phase: " + phase);
        }
        
        // Get or create phase data
        PhaseData phaseData = phaseStorage.computeIfAbsent(phoneNumber, k -> new PhaseData());
        
        // Check if this is the correct phase
        String expectedPhase = getExpectedPhase(phaseData);
        if (!phase.equals(expectedPhase)) {
            return PhaseVerificationResponse.builder()
                    .phase(phase)
                    .verified(false)
                    .confidence(0.0f)
                    .message("Sai thứ tự phase. Phase hiện tại phải là: " + expectedPhase)
                    .nextPhase(expectedPhase)
                    .build();
        }
        
        // Call Python service to verify action
        Map<String, Object> result = callPythonVerifyAction(image, phase);
        
        boolean verified = (boolean) result.get("verified");
        double confidence = ((Number) result.get("confidence")).doubleValue();
        String reason = (String) result.get("reason");
        
        if (verified) {
            // Mark phase as complete
            phaseData.completePhase(phase, confidence, reason);
            
            // Get next phase
            String nextPhase = getNextPhase(phase);
            
            String message;
            if (nextPhase != null) {
                message = "Phase " + phase + " hoàn thành! Tiếp tục với phase tiếp theo.";
            } else {
                message = "Tất cả phases đã hoàn thành! Đang xác thực cuối cùng...";
            }
            
            return PhaseVerificationResponse.builder()
                    .phase(phase)
                    .verified(true)
                    .confidence((float) confidence)
                    .message(message)
                    .nextPhase(nextPhase)
                    .reason(reason)
                    .build();
        } else {
            return PhaseVerificationResponse.builder()
                    .phase(phase)
                    .verified(false)
                    .confidence((float) confidence)
                    .message("Xác thực thất bại. Vui lòng thử lại.")
                    .nextPhase(phase) // Retry same phase
                    .reason(reason)
                    .build();
        }
    }
    
    @Override
    public PhaseVerificationResponse getCurrentPhaseStatus(String phoneNumber) {
        PhaseData phaseData = phaseStorage.get(phoneNumber);
        
        if (phaseData == null) {
            return PhaseVerificationResponse.builder()
                    .phase("LOOK_STRAIGHT")
                    .verified(false)
                    .message("Chưa bắt đầu liveness check")
                    .nextPhase("LOOK_STRAIGHT")
                    .build();
        }
        
        String currentPhase = getExpectedPhase(phaseData);
        boolean allComplete = phaseData.isAllPhasesComplete();
        
        return PhaseVerificationResponse.builder()
                .phase(currentPhase)
                .verified(allComplete)
                .message(allComplete ? "Tất cả phases hoàn thành" : "Đang ở phase: " + currentPhase)
                .nextPhase(allComplete ? null : currentPhase)
                .build();
    }
    
    @Override
    public void resetLivenessCheck(String phoneNumber) {
        phaseStorage.remove(phoneNumber);
        log.info("Reset liveness check for phone: {}", phoneNumber);
    }
    
    private Map<String, Object> callPythonVerifyAction(MultipartFile image, String action) throws Exception {
        String url = faceServiceUrl + "/verify-action";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        });
        body.add("action", action);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling Python verify-action service: {}", e.getMessage());
            throw new Exception("Không thể kết nối đến dịch vụ xác thực AI", e);
        }
    }
    
    private boolean isValidPhase(String phase) {
        for (String validPhase : PHASE_ORDER) {
            if (validPhase.equals(phase)) {
                return true;
            }
        }
        return false;
    }
    
    private String getExpectedPhase(PhaseData phaseData) {
        for (String phase : PHASE_ORDER) {
            if (!phaseData.isPhaseComplete(phase)) {
                return phase;
            }
        }
        return null; // All phases complete
    }
    
    private String getNextPhase(String currentPhase) {
        for (int i = 0; i < PHASE_ORDER.length - 1; i++) {
            if (PHASE_ORDER[i].equals(currentPhase)) {
                return PHASE_ORDER[i + 1];
            }
        }
        return null; // Last phase
    }
    
    // Inner class to store phase data
    private static class PhaseData {
        private final Map<String, PhaseResult> phases = new HashMap<>();
        
        public void completePhase(String phase, double confidence, String reason) {
            phases.put(phase, new PhaseResult(true, confidence, reason));
        }
        
        public boolean isPhaseComplete(String phase) {
            PhaseResult result = phases.get(phase);
            return result != null && result.verified;
        }
        
        public boolean isAllPhasesComplete() {
            return phases.size() == PHASE_ORDER.length && 
                   phases.values().stream().allMatch(r -> r.verified);
        }
    }
    
    private static class PhaseResult {
        boolean verified;
        double confidence;
        String reason;
        
        PhaseResult(boolean verified, double confidence, String reason) {
            this.verified = verified;
            this.confidence = confidence;
            this.reason = reason;
        }
    }
}

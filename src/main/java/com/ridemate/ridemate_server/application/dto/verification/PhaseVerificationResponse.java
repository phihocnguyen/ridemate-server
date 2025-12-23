package com.ridemate.ridemate_server.application.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhaseVerificationResponse {
    private String phase; // LOOK_STRAIGHT, BLINK, TURN_LEFT
    private Boolean verified;
    private Float confidence;
    private String message;
    private String nextPhase; // null if this is the last phase
    private String reason; // Detailed reason from Gemini
}

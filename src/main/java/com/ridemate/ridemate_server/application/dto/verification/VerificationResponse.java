package com.ridemate.ridemate_server.application.dto.verification;

import com.ridemate.ridemate_server.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    private User.VerificationStatus status;
    private String message;
    private Float similarityScore;
    private String idCardImageUrl;
    private boolean verified;
}

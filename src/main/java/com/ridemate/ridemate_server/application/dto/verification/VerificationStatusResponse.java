package com.ridemate.ridemate_server.application.dto.verification;

import com.ridemate.ridemate_server.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatusResponse {
    private User.VerificationStatus verificationStatus;
    private boolean idCardUploaded;
    private boolean livenessChecked;
    private boolean verified;
    private LocalDateTime verificationDate;
    private Float similarityScore;
}

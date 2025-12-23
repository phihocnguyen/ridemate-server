package com.ridemate.ridemate_server.application.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LivenessCheckRequest {
    private String phoneNumber;
    // Note: The actual selfie image will be received as MultipartFile in the controller
}

package com.ridemate.ridemate_server.application.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdCardUploadRequest {
    private String phoneNumber;
    // Note: The actual image file will be received as MultipartFile in the controller
}

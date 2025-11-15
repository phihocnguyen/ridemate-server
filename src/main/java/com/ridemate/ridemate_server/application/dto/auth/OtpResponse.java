package com.ridemate.ridemate_server.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP response")
public class OtpResponse {

    @Schema(description = "OTP sent successfully", example = "true")
    private Boolean success;

    @Schema(description = "Message about OTP", example = "OTP sent to 0912345678")
    private String message;

    @Schema(description = "Expiry time of OTP", example = "2025-11-15T10:38:17.209+07:00")
    private LocalDateTime expiryTime;

    @Schema(description = "Identifier/phone number that received OTP", example = "0912345678")
    private String identifier;
}

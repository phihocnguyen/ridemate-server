package com.ridemate.ridemate_server.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Send OTP request")
public class SendOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    @Schema(description = "Phone number to send OTP", example = "0912345678")
    private String phoneNumber;

    @NotBlank(message = "Purpose is required")
    @Schema(description = "Purpose of OTP (REGISTER, LOGIN, RESET_PASS)", 
            example = "REGISTER", 
            allowableValues = {"REGISTER", "LOGIN", "RESET_PASS"})
    private String purpose;
}

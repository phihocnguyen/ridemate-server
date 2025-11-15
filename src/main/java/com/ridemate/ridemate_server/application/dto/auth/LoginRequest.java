package com.ridemate.ridemate_server.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Phone number is required")
    @Schema(description = "User's phone number", example = "0912345678")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "Password@123")
    private String password;
}

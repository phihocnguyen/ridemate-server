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
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Schema(description = "User's full name", example = "Nguyễn Văn A")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    @Schema(description = "User's phone number", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password (min 8 chars)", example = "Password@123")
    private String password;

    @NotBlank(message = "User type is required")
    @Schema(description = "User type (DRIVER or PASSENGER)", example = "PASSENGER", allowableValues = {"DRIVER", "PASSENGER"})
    private String userType;

    @Schema(description = "User's profile picture URL", example = "https://ociacc.com/wp-content/uploads/2019/03/blank-profile-picture-973460_1280.png")
    private String profilePictureUrl;

    @Schema(description = "Face ID data for biometric authentication (base64 encoded)", example = "base64_face_data")
    private String faceIdData;

    @Schema(description = "Current latitude for location tracking", example = "20.9852")
    private Double currentLatitude;

    @Schema(description = "Current longitude for location tracking", example = "105.7625")
    private Double currentLongitude;
}

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
@Schema(description = "Social login request for Google and Facebook")
public class SocialLoginRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "OAuth2 token from Google or Facebook", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @NotBlank(message = "Provider is required")
    @Schema(description = "OAuth2 provider", example = "GOOGLE", allowableValues = {"GOOGLE", "FACEBOOK"})
    private String provider;

    @Schema(description = "Facebook App Access Token (required for Facebook)", example = "your_app_access_token")
    private String appAccessToken;

    @Schema(description = "User's full name from social profile", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "User type (DRIVER or PASSENGER)", example = "PASSENGER", allowableValues = {"DRIVER", "PASSENGER"})
    private String userType;

    @Schema(description = "User's latitude for initial location", example = "20.9852")
    private Double currentLatitude;

    @Schema(description = "User's longitude for initial location", example = "105.7625")
    private Double currentLongitude;
}

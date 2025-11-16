package com.ridemate.ridemate_server.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile data")
public class UserDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User's full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "User's phone number", example = "0912345678")
    private String phoneNumber;

    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Schema(description = "User's profile picture URL", example = "https://example.com/profile.jpg")
    private String profilePictureUrl;

    @Schema(description = "User's average rating", example = "4.5")
    private Float rating;

    @Schema(description = "User's reward points", example = "100")
    private Integer points;

    @Schema(description = "User type (DRIVER or PASSENGER)", example = "PASSENGER")
    private String userType;

    @Schema(description = "User's authentication provider", example = "LOCAL")
    private String authProvider;
}
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

    @Schema(description = "User's profile picture URL", example = "https://ociacc.com/wp-content/uploads/2019/03/blank-profile-picture-973460_1280.png")
    private String profilePictureUrl;

    @Schema(description = "User's date of birth", example = "1990-01-01")
    private java.time.LocalDate dob;

    @Schema(description = "User's address", example = "123 Nguyen Hue, District 1, HCMC")
    private String address;

    @Schema(description = "Bank name", example = "Vietcombank")
    private String bankName;

    @Schema(description = "Bank account number", example = "1234567890")
    private String bankAccountNumber;

    @Schema(description = "User's reward points", example = "100")
    private Integer coins;

    @Schema(description = "Total rides completed by user", example = "25")
    private Integer totalRides;

    @Schema(description = "User type (DRIVER or PASSENGER)", example = "PASSENGER")
    private String userType;

    @Schema(description = "User's authentication provider", example = "LOCAL")
    private String authProvider;
}
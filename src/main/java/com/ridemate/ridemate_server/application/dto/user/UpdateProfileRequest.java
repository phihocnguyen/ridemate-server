package com.ridemate.ridemate_server.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {
    
    @Schema(description = "User's full name", example = "Nguyễn Văn A")
    private String fullName;
    
    @Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;
    
    @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Phone number must be valid Vietnamese format")
    @Schema(description = "User's phone number", example = "0929822968")
    private String phoneNumber;
    
    @Schema(description = "User's date of birth", example = "1990-01-01")
    private LocalDate dob;
    
    @Schema(description = "User's address", example = "123 Nguyen Hue, District 1, HCMC")
    private String address;
    
    @Schema(description = "Bank name", example = "Vietcombank")
    private String bankName;
    
    @Schema(description = "Bank account number", example = "1234567890")
    private String bankAccountNumber;
    
    @Schema(description = "Profile picture URL", example = "https://cloudinary.com/image.jpg")
    private String profilePictureUrl;
}

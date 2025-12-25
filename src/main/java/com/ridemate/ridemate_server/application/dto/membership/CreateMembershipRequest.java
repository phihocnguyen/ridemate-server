package com.ridemate.ridemate_server.application.dto.membership;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new membership package")
public class CreateMembershipRequest {
    
    @NotBlank(message = "Membership ID is required")
    @Size(max = 50, message = "Membership ID must not exceed 50 characters")
    @Schema(description = "Membership package ID (e.g., MEM-1001)", example = "MEM-1001", required = true)
    private String membershipId;
    
    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Schema(description = "Membership name", example = "RideMate Premium", required = true)
    private String name;
    
    @NotBlank(message = "Description is required")
    @Schema(description = "Membership description", example = "Ưu đãi đặc biệt mọi chuyến xe", required = true)
    private String description;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be non-negative")
    @Schema(description = "Price in VND", example = "199000", required = true)
    private Integer price;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Schema(description = "Duration in days", example = "30", required = true)
    private Integer duration;
    
    @NotNull(message = "Max trips per day is required")
    @Min(value = 1, message = "Max trips per day must be at least 1")
    @Schema(description = "Maximum trips per day", example = "5", required = true)
    private Integer maxTripsPerDay;
    
    @NotNull(message = "Point multiplier is required")
    @Min(value = 1, message = "Point multiplier must be at least 1.0")
    @Schema(description = "Point multiplier", example = "2.0", required = true)
    private Double pointMultiplier;
    
    @Schema(description = "List of benefits", example = "[\"Giảm 10% mọi chuyến đi\", \"Tích điểm x2\"]")
    private List<String> benefits;
    
    @Schema(description = "Status (ACTIVE or PAUSED)", example = "ACTIVE")
    @Builder.Default
    private String status = "ACTIVE";
}


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
@Schema(description = "Request to update a membership package")
public class UpdateMembershipRequest {
    
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Schema(description = "Membership name", example = "RideMate Premium")
    private String name;
    
    @Schema(description = "Membership description", example = "Ưu đãi đặc biệt mọi chuyến xe")
    private String description;
    
    @Min(value = 0, message = "Price must be non-negative")
    @Schema(description = "Price in VND", example = "199000")
    private Integer price;
    
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Schema(description = "Duration in days", example = "30")
    private Integer duration;
    
    @Min(value = 1, message = "Max trips per day must be at least 1")
    @Schema(description = "Maximum trips per day", example = "5")
    private Integer maxTripsPerDay;
    
    @Min(value = 1, message = "Point multiplier must be at least 1.0")
    @Schema(description = "Point multiplier", example = "2.0")
    private Double pointMultiplier;
    
    @Schema(description = "List of benefits", example = "[\"Giảm 10% mọi chuyến đi\", \"Tích điểm x2\"]")
    private List<String> benefits;
    
    @Schema(description = "Status (ACTIVE, PAUSED, or DELETED)", example = "ACTIVE")
    private String status;
}


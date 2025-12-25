package com.ridemate.ridemate_server.application.dto.membership;

import com.ridemate.ridemate_server.domain.entity.Membership;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Membership package response")
public class MembershipResponse {
    
    @Schema(description = "Membership ID")
    private Long id;
    
    @Schema(description = "Membership package ID (e.g., MEM-1001)")
    private String membershipId;
    
    @Schema(description = "Membership name")
    private String name;
    
    @Schema(description = "Membership description")
    private String description;
    
    @Schema(description = "Price in VND")
    private Integer price;
    
    @Schema(description = "Duration in days")
    private Integer duration;
    
    @Schema(description = "Maximum trips per day")
    private Integer maxTripsPerDay;
    
    @Schema(description = "Point multiplier")
    private Double pointMultiplier;
    
    @Schema(description = "List of benefits")
    private List<String> benefits;
    
    @Schema(description = "Status (ACTIVE, PAUSED, DELETED)")
    private String status;
    
    @Schema(description = "Number of active subscribers")
    private Long subscribers;
    
    @Schema(description = "Created at")
    private LocalDateTime createdAt;
    
    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;
}


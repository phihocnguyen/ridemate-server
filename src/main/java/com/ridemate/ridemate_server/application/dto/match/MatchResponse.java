package com.ridemate.ridemate_server.application.dto.match;

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
@Schema(description = "Match/Ride details response")
public class MatchResponse {
    private Long id;
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    
    private Long driverId;
    private String driverName;
    private String driverPhone;
    
    private Long vehicleId;
    private String vehicleInfo; // "Honda Wave - 59A1-12345"

    private String pickupAddress;
    private String destinationAddress;
    
    @Schema(description = "Coin cost calculated based on distance", example = "25")
    private Integer coin;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    // ✅ NEW: List of matched driver candidates
    @Schema(description = "List of available drivers for this match")
    private List<DriverCandidate> matchedDriverCandidates;
    
    // ✅ NEW: Optional message for additional info
    @Schema(description = "Additional message (e.g. 'No drivers available')")
    private String message;
}
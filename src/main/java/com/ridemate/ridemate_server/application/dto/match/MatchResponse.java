package com.ridemate.ridemate_server.application.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
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
    private Double fare;
    private String status;
    
    private LocalDateTime createdAt;
}
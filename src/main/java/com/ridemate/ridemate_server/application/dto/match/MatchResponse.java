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
    
    // Pickup and destination coordinates for map routing
    @Schema(description = "Pickup location latitude", example = "10.7769")
    private Double pickupLatitude;
    
    @Schema(description = "Pickup location longitude", example = "106.7009")
    private Double pickupLongitude;
    
    @Schema(description = "Destination location latitude", example = "10.8231")
    private Double destinationLatitude;
    
    @Schema(description = "Destination location longitude", example = "106.6297")
    private Double destinationLongitude;
    
    @Schema(description = "Distance in meters", example = "5000")
    private Double distance;
    
    @Schema(description = "Duration in minutes", example = "15")
    private Integer duration;
    
    @Schema(description = "Coin cost calculated based on distance", example = "25")
    private Integer coin;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private Long sessionId;
    
    private Double driverCurrentLatitude;
    private Double driverCurrentLongitude;

    // ✅ NEW: List of matched driver candidates
    @Schema(description = "List of available drivers for this match")
    private List<DriverCandidate> matchedDriverCandidates;
    
    // ✅ NEW: Optional message for additional info
    @Schema(description = "Additional message (e.g. 'No drivers available')")
    private String message;

    // Fields for broadcast matching
    private String passengerAvatar;
    private Double passengerRating;
    private Integer passengerReviews;

    private String driverAvatar;
    private Double driverRating;
    private String vehicleModel;
    private String licensePlate;
    private Integer estimatedPrice;

    @Schema(description = "Encoded polyline string of the route (from OSRM Directions API)")
    private String routePolyline;

    @Schema(description = "Associated route booking details if applicable")
    private com.ridemate.ridemate_server.application.dto.route.RouteBookingResponse routeBooking;
}
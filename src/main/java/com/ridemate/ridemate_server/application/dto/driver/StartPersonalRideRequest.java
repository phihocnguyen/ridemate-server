package com.ridemate.ridemate_server.application.dto.driver;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartPersonalRideRequest {
    
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;
    
    @NotNull(message = "Destination latitude is required")
    private Double destinationLatitude;
    
    @NotNull(message = "Destination longitude is required")
    private Double destinationLongitude;
    
    private String pickupAddress;
    private String destinationAddress;
    
    // Optional: passenger info if driver is taking someone
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
}

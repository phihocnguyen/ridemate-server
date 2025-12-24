package com.ridemate.ridemate_server.application.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request to book a ride")
public class BookRideRequest {

    @NotBlank(message = "Pickup address is required")
    @Schema(example = "123 Nguyen Hue, District 1")
    private String pickupAddress;

    @NotBlank(message = "Destination address is required")
    @Schema(example = "456 Le Loi, District 1")
    private String destinationAddress;

    @Schema(example = "10.7769")
    private Double pickupLatitude;

    @Schema(example = "106.7009")
    private Double pickupLongitude;

    @Schema(example = "10.7721")
    private Double destinationLatitude;

    @Schema(example = "106.6983")
    private Double destinationLongitude;

    @NotBlank(message = "Vehicle type is required")
    @Schema(example = "MOTORBIKE", allowableValues = {"MOTORBIKE", "CAR"})
    private String vehicleType;

    @Schema(description = "Encoded polyline string of the route (from OSRM Directions API)", example = "encoded_polyline_string")
    private String routePolyline;

    @Schema(description = "Distance in meters", example = "5000")
    private Integer distance;

    @Schema(description = "Duration in minutes", example = "15")
    private Integer duration;
}
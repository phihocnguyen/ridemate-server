package com.ridemate.ridemate_server.application.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to search for fixed routes")
public class SearchFixedRoutesRequest {

    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "User's pickup latitude", example = "10.7726")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "User's pickup longitude", example = "106.6980")
    private Double pickupLongitude;

    @NotNull(message = "Dropoff latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "User's dropoff latitude", example = "10.8808")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "User's dropoff longitude", example = "106.8069")
    private Double dropoffLongitude;

    @Schema(description = "Pickup address (optional)", example = "Near Bến Thành Market")
    private String pickupAddress;

    @Schema(description = "Dropoff address (optional)", example = "Near HCMC University")
    private String dropoffAddress;

    @Schema(description = "Desired travel date (optional, defaults to today)", example = "2025-12-23")
    private LocalDate travelDate;

    @Min(value = 1, message = "Number of seats must be at least 1")
    @Schema(description = "Number of seats needed", example = "1")
    private Integer numberOfSeats;
}


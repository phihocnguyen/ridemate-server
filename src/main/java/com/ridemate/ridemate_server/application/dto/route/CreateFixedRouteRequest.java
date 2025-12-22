package com.ridemate.ridemate_server.application.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new fixed route")
public class CreateFixedRouteRequest {

    @NotNull(message = "Vehicle ID is required")
    @Schema(description = "ID of the vehicle to use for this route", example = "1")
    private Long vehicleId;

    @NotBlank(message = "Route name is required")
    @Size(max = 200, message = "Route name must not exceed 200 characters")
    @Schema(description = "Name of the route", example = "Quận 1 → Thủ Đức")
    private String routeName;

    @Schema(description = "Description of the route", example = "Daily commute route from downtown to university area")
    private String description;

    // Pickup point
    @NotBlank(message = "Pickup address is required")
    @Schema(description = "Pickup address", example = "Bến Thành Market, District 1, HCMC")
    private String pickupAddress;

    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Pickup latitude", example = "10.7726")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Pickup longitude", example = "106.6980")
    private Double pickupLongitude;

    // Dropoff point
    @NotBlank(message = "Dropoff address is required")
    @Schema(description = "Dropoff address", example = "HCMC University of Technology, Thu Duc City")
    private String dropoffAddress;

    @NotNull(message = "Dropoff latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Dropoff latitude", example = "10.8808")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Dropoff longitude", example = "106.8069")
    private Double dropoffLongitude;

    // Schedule
    @NotNull(message = "Departure time is required")
    @Schema(description = "Scheduled departure time", example = "07:30:00")
    private LocalTime departureTime;

    @NotBlank(message = "Specific dates is required")
    @Schema(description = "Specific dates for this route (comma-separated in yyyy-MM-dd format)", example = "2025-12-22,2025-12-23,2025-12-24", required = true)
    private String specificDates;

    // Pricing
    @NotNull(message = "Price per seat is required")
    @Min(value = 0, message = "Price must be non-negative")
    @Schema(description = "Price per seat in VND", example = "20000")
    private Integer pricePerSeat;

    // Capacity
    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 10, message = "Total seats cannot exceed 10")
    @Schema(description = "Total seats available", example = "2")
    private Integer totalSeats;

    // Proximity radius (optional, defaults will be used if not provided)
    @Min(value = 100, message = "Pickup radius must be at least 100 meters")
    @Max(value = 5000, message = "Pickup radius cannot exceed 5000 meters")
    @Schema(description = "Pickup radius in meters for matching", example = "500")
    private Integer pickupRadius;

    @Min(value = 100, message = "Dropoff radius must be at least 100 meters")
    @Max(value = 5000, message = "Dropoff radius cannot exceed 5000 meters")
    @Schema(description = "Dropoff radius in meters for matching", example = "500")
    private Integer dropoffRadius;
}


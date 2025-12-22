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
@Schema(description = "Request to update an existing fixed route")
public class UpdateFixedRouteRequest {

    @Schema(description = "Route name", example = "Quận 1 → Thủ Đức (Updated)")
    @Size(max = 200, message = "Route name must not exceed 200 characters")
    private String routeName;

    @Schema(description = "Description of the route")
    private String description;

    @Schema(description = "Scheduled departure time", example = "08:00:00")
    private LocalTime departureTime;

    @Schema(description = "Specific dates for this route (comma-separated in yyyy-MM-dd format)", example = "2025-12-22,2025-12-23")
    private String specificDates;

    @Min(value = 0, message = "Price must be non-negative")
    @Schema(description = "Price per seat in VND", example = "25000")
    private Integer pricePerSeat;

    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 10, message = "Total seats cannot exceed 10")
    @Schema(description = "Total seats available", example = "3")
    private Integer totalSeats;

    @Min(value = 100, message = "Pickup radius must be at least 100 meters")
    @Max(value = 5000, message = "Pickup radius cannot exceed 5000 meters")
    @Schema(description = "Pickup radius in meters for matching", example = "500")
    private Integer pickupRadius;

    @Min(value = 100, message = "Dropoff radius must be at least 100 meters")
    @Max(value = 5000, message = "Dropoff radius cannot exceed 5000 meters")
    @Schema(description = "Dropoff radius in meters for matching", example = "500")
    private Integer dropoffRadius;

    @Schema(description = "Route status", example = "ACTIVE")
    private String status; // ACTIVE, INACTIVE, COMPLETED, CANCELLED
}


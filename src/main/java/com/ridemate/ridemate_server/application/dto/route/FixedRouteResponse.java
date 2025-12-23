package com.ridemate.ridemate_server.application.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fixed route details response")
public class FixedRouteResponse {

    @Schema(description = "Route ID", example = "1")
    private Long id;

    @Schema(description = "Driver ID", example = "5")
    private Long driverId;

    @Schema(description = "Driver name", example = "Nguyễn Văn A")
    private String driverName;

    @Schema(description = "Driver phone", example = "0901234567")
    private String driverPhone;

    @Schema(description = "Driver avatar URL")
    private String driverAvatar;

    @Schema(description = "Driver rating", example = "4.8")
    private Float driverRating;

    @Schema(description = "Vehicle ID", example = "3")
    private Long vehicleId;

    @Schema(description = "Vehicle info", example = "Honda Wave - 59A1-12345")
    private String vehicleInfo;

    @Schema(description = "Vehicle model", example = "Honda Wave")
    private String vehicleModel;

    @Schema(description = "License plate", example = "59A1-12345")
    private String licensePlate;

    @Schema(description = "Route name", example = "Quận 1 → Thủ Đức")
    private String routeName;

    @Schema(description = "Route description")
    private String description;

    // Pickup point
    @Schema(description = "Pickup address", example = "Bến Thành Market, District 1")
    private String pickupAddress;

    @Schema(description = "Pickup latitude", example = "10.7726")
    private Double pickupLatitude;

    @Schema(description = "Pickup longitude", example = "106.6980")
    private Double pickupLongitude;

    // Dropoff point
    @Schema(description = "Dropoff address", example = "HCMC University of Technology")
    private String dropoffAddress;

    @Schema(description = "Dropoff latitude", example = "10.8808")
    private Double dropoffLatitude;

    @Schema(description = "Dropoff longitude", example = "106.8069")
    private Double dropoffLongitude;

    // Schedule
    @Schema(description = "Departure time", example = "07:30:00")
    private LocalTime departureTime;

    @Schema(description = "Specific dates for this route (comma-separated in yyyy-MM-dd format)", example = "2025-12-22,2025-12-23")
    private String specificDates;

    // Pricing and capacity
    @Schema(description = "Price per seat in VND", example = "20000")
    private Integer pricePerSeat;

    @Schema(description = "Total seats", example = "2")
    private Integer totalSeats;

    @Schema(description = "Available seats", example = "1")
    private Integer availableSeats;

    @Schema(description = "Distance in meters", example = "15000")
    private Double distance;

    @Schema(description = "Route status", example = "ACTIVE")
    private String status;

    @Schema(description = "Pickup radius in meters", example = "500")
    private Integer pickupRadius;

    @Schema(description = "Dropoff radius in meters", example = "500")
    private Integer dropoffRadius;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at")
    private LocalDateTime updatedAt;

    // Distance from user's search location (optional, calculated during search)
    @Schema(description = "Distance from user's pickup location to route's pickup point (meters)")
    private Double pickupDistanceFromUser;

    @Schema(description = "Distance from user's dropoff location to route's dropoff point (meters)")
    private Double dropoffDistanceFromUser;
}


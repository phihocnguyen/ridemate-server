package com.ridemate.ridemate_server.application.dto.route;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Route booking details response")
public class RouteBookingResponse {

    @Schema(description = "Booking ID", example = "1")
    private Long id;

    @Schema(description = "Route ID", example = "5")
    private Long routeId;

    @Schema(description = "Route name", example = "Quận 1 → Thủ Đức")
    private String routeName;

    @Schema(description = "Passenger ID", example = "10")
    private Long passengerId;

    @Schema(description = "Passenger name", example = "Trần Thị B")
    private String passengerName;

    @Schema(description = "Passenger phone", example = "0912345678")
    private String passengerPhone;

    @Schema(description = "Passenger avatar URL")
    private String passengerAvatar;

    @Schema(description = "Passenger rating", example = "4.5")
    private Float passengerRating;

    @Schema(description = "Driver ID", example = "3")
    private Long driverId;

    @Schema(description = "Driver name", example = "Nguyễn Văn A")
    private String driverName;

    @Schema(description = "Driver phone", example = "0901234567")
    private String driverPhone;

    // Passenger's pickup location
    @Schema(description = "Pickup address", example = "123 Lê Lợi, District 1")
    private String pickupAddress;

    @Schema(description = "Pickup latitude", example = "10.7730")
    private Double pickupLatitude;

    @Schema(description = "Pickup longitude", example = "106.6985")
    private Double pickupLongitude;

    // Passenger's dropoff location
    @Schema(description = "Dropoff address", example = "268 Lý Thường Kiệt, Thu Duc")
    private String dropoffAddress;

    @Schema(description = "Dropoff latitude", example = "10.8810")
    private Double dropoffLatitude;

    @Schema(description = "Dropoff longitude", example = "106.8070")
    private Double dropoffLongitude;

    @Schema(description = "Booking date", example = "2025-12-23")
    private LocalDate bookingDate;

    @Schema(description = "Number of seats booked", example = "1")
    private Integer numberOfSeats;

    @Schema(description = "Total price in VND", example = "20000")
    private Integer totalPrice;

    @Schema(description = "Distance from route pickup point (meters)", example = "150")
    private Double pickupDistanceFromRoute;

    @Schema(description = "Distance from route dropoff point (meters)", example = "200")
    private Double dropoffDistanceFromRoute;

    @Schema(description = "Booking status", example = "PENDING")
    private String status;

    @Schema(description = "Match ID (if trip has started)", example = "25")
    private Long matchId;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

    @Schema(description = "Accepted at")
    private LocalDateTime acceptedAt;

    @Schema(description = "Rejected at")
    private LocalDateTime rejectedAt;

    @Schema(description = "Cancelled at")
    private LocalDateTime cancelledAt;

    @Schema(description = "Completed at")
    private LocalDateTime completedAt;

    // Route details for display
    @Schema(description = "Route pickup address")
    private String routePickupAddress;

    @Schema(description = "Route dropoff address")
    private String routeDropoffAddress;

    @Schema(description = "Route departure time", example = "07:30:00")
    private String routeDepartureTime;

    @Schema(description = "Vehicle info", example = "Honda Wave - 59A1-12345")
    private String vehicleInfo;
}


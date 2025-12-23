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
@Schema(description = "Request to book a seat on a fixed route")
public class CreateRouteBookingRequest {

    @NotNull(message = "Route ID is required")
    @Schema(description = "ID of the fixed route to book", example = "1")
    private Long routeId;

    @NotNull(message = "Booking date is required")
    @Schema(description = "Date for the booking", example = "2025-12-23")
    private LocalDate bookingDate;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Number of seats must be at least 1")
    @Max(value = 5, message = "Cannot book more than 5 seats at once")
    @Schema(description = "Number of seats to book", example = "1")
    private Integer numberOfSeats;

    // Passenger's actual pickup location
    @NotBlank(message = "Pickup address is required")
    @Schema(description = "Passenger's pickup address", example = "123 Lê Lợi, District 1")
    private String pickupAddress;

    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Pickup latitude", example = "10.7730")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Pickup longitude", example = "106.6985")
    private Double pickupLongitude;

    // Passenger's actual dropoff location
    @NotBlank(message = "Dropoff address is required")
    @Schema(description = "Passenger's dropoff address", example = "268 Lý Thường Kiệt, Thu Duc")
    private String dropoffAddress;

    @NotNull(message = "Dropoff latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Schema(description = "Dropoff latitude", example = "10.8810")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Schema(description = "Dropoff longitude", example = "106.8070")
    private Double dropoffLongitude;
}


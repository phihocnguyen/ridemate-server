package com.ridemate.ridemate_server.application.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vehicle information response")
public class VehicleResponse {

    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @Schema(description = "Driver ID", example = "1")
    private Long driverId;

    @Schema(description = "Driver full name", example = "Nguyễn Văn A")
    private String driverName;

    @Schema(description = "License plate", example = "30A-12345")
    private String licensePlate;

    @Schema(description = "Vehicle make/brand", example = "Honda")
    private String make;

    @Schema(description = "Vehicle model", example = "Wave RSX")
    private String model;

    @Schema(description = "Vehicle color", example = "Đỏ")
    private String color;

    @Schema(description = "Vehicle capacity", example = "2")
    private Integer capacity;

    @Schema(description = "Vehicle type", example = "MOTORBIKE")
    private String vehicleType;

    @Schema(description = "Registration document URL", example = "https://res.cloudinary.com/...")
    private String registrationDocumentUrl;

    @Schema(description = "License plate image URL", example = "https://res.cloudinary.com/...")
    private String licensePlateImageUrl;

    @Schema(description = "Vehicle status", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED", "INACTIVE"})
    private String status;

    @Schema(description = "Created at timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Updated at timestamp")
    private LocalDateTime updatedAt;
}


package com.ridemate.ridemate_server.application.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Vehicle registration request")
public class RegisterVehicleRequest {

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[0-9]{2}-[A-Z0-9]{1,3}[\\s-]([0-9]{4,5}|[0-9]{3}\\.[0-9]{2})$", message = "License plate must be in format: XX-XX YYYY (e.g., 29-B1 12345) or XX-XX YYY.YY (e.g., 69-D1 666.66)")
    @Schema(description = "Vehicle license plate", example = "30A-12345")
    private String licensePlate;

    @NotBlank(message = "Make is required")
    @Schema(description = "Vehicle make/brand", example = "Honda")
    private String make;

    @NotBlank(message = "Model is required")
    @Schema(description = "Vehicle model", example = "Wave RSX")
    private String model;

    @NotBlank(message = "Color is required")
    @Schema(description = "Vehicle color", example = "Đỏ")
    private String color;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Vehicle capacity (number of passengers)", example = "2")
    private Integer capacity;

    @NotBlank(message = "Vehicle type is required")
    @Schema(description = "Vehicle type", example = "MOTORBIKE", allowableValues = {"MOTORBIKE", "CAR", "VAN", "TRUCK"})
    private String vehicleType;

    @NotBlank(message = "Registration document URL is required")
    @Schema(description = "URL of the registration document (uploaded via /upload endpoint)", example = "https://res.cloudinary.com/...")
    private String registrationDocumentUrl;

    @Schema(description = "URL of the license plate image (optional)", example = "https://res.cloudinary.com/...")
    private String licensePlateImageUrl;
}


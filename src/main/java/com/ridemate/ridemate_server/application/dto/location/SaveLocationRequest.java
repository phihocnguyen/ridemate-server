package com.ridemate.ridemate_server.application.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to save a favorite location")
public class SaveLocationRequest {

    @NotBlank(message = "Name is required")
    @Schema(example = "Home")
    private String name;

    @NotBlank(message = "Address is required")
    @Schema(example = "123 Nguyen Hue, District 1, HCMC")
    private String address;

    @NotNull(message = "Latitude is required")
    @Schema(example = "10.7769")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Schema(example = "106.7009")
    private Double longitude;
}
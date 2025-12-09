package com.ridemate.ridemate_server.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update driver status and location")
public class UpdateDriverStatusRequest {

    @Schema(description = "Driver status (ONLINE, OFFLINE, BUSY)", example = "ONLINE", allowableValues = {"ONLINE", "OFFLINE", "BUSY"})
    private String status;

    @Schema(description = "Current latitude", example = "10.7769")
    private Double latitude;

    @Schema(description = "Current longitude", example = "106.7009")
    private Double longitude;
}

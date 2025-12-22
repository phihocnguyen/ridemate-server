package com.ridemate.ridemate_server.application.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update vehicle status (for admin)")
public class UpdateVehicleStatusRequest {

    @NotBlank(message = "Status is required")
    @Schema(description = "New vehicle status", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED", "INACTIVE"})
    private String status;
    
    @Schema(description = "Reason for rejection (optional, used when status is REJECTED)", example = "Invalid registration document")
    private String rejectionReason;
}


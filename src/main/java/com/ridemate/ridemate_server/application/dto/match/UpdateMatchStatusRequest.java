package com.ridemate.ridemate_server.application.dto.match;

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
@Schema(description = "Request to update match status")
public class UpdateMatchStatusRequest {

    @NotBlank(message = "Status is required")
    @Schema(description = "New match status", example = "IN_PROGRESS", allowableValues = {"IN_PROGRESS", "COMPLETED", "CANCELLED"})
    private String status;
}
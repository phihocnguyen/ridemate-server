package com.ridemate.ridemate_server.application.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Session creation request")
public class SessionRequest {
    
    @NotNull(message = "Match ID is required")
    @Schema(description = "ID of the match to start session for", example = "1")
    private Long matchId;
    
    @Schema(description = "Session start time (defaults to current time if not provided)")
    private LocalDateTime startTime;
}

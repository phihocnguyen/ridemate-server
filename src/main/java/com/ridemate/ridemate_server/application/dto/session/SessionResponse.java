package com.ridemate.ridemate_server.application.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Session details response")
public class SessionResponse {
    
    @Schema(description = "Session ID", example = "1")
    private Long id;
    
    @Schema(description = "Associated Match ID", example = "1")
    private Long matchId;
    
    @Schema(description = "Session start time")
    private LocalDateTime startTime;
    
    @Schema(description = "Session end time (null if still active)")
    private LocalDateTime endTime;
    
    @Schema(description = "Is session currently active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Session creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Session last update timestamp")
    private LocalDateTime updatedAt;
}

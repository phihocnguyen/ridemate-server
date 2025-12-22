package com.ridemate.ridemate_server.application.dto.report;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportActionRequest {
    @NotNull(message = "Action type is required")
    private ReportActionType actionType;
    
    private String reason;
}

package com.ridemate.ridemate_server.application.dto.report;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportStatusRequest {
    
    @NotBlank(message = "Status is required")
    private String status; // PENDING, PROCESSING, RESOLVED, REJECTED
    
    private String resolutionAction; // WARNING, LOCK_7_DAYS, LOCK_30_DAYS, LOCK_PERMANENT
    
    private String resolutionNotes;
}

package com.ridemate.ridemate_server.application.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResolutionResponse {
    private Long reportId;
    private String status;
    private ReportActionType actionType;
    private String actionDescription;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    
    // Information about the affected user
    private Long reportedUserId;
    private String reportedUserName;
    private Integer userWarningCount;
    private LocalDateTime userLockedUntil;
    private String userLockMessage;
}

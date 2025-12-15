package com.ridemate.ridemate_server.presentation.dto.admin;

import com.ridemate.ridemate_server.domain.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportManagementDto {
    private Long id;
    private UserInfo reporter;
    private UserInfo reportedUser;
    private Long matchId;
    private String title;
    private String description;
    private Report.ReportCategory category;
    private Report.ReportStatus status;
    private String evidenceUrl;
    private Report.ResolutionAction resolutionAction;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String phoneNumber;
        private String profilePictureUrl;
    }
}

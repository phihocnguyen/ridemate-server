package com.ridemate.ridemate_server.application.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportManagementDto {
    private Long id;
    private UserInfo reporter;
    private String reporterName; 
    private UserInfo reportedUser;
    private Long matchId;
    private String title;
    private String description;
    private String category; 
    private String status;
    private String evidenceUrl;
    private String resolutionAction;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Session messages for admin review
    private List<SessionMessageDto> sessionMessages;
}

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

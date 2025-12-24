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
    private Long reporterId;
    private String reporterName;
    private String reporterPhone;
    private Long reportedUserId;
    private String reportedUserName;
    private String reportedUserPhone;
    private Long matchId;
    private String title;
    private String description;
    private String category;
    private String status;
    private String evidenceUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Session messages for admin review
    private List<SessionMessageDto> sessionMessages;
}

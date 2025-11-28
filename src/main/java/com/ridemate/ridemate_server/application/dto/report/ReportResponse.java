package com.ridemate.ridemate_server.application.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Report details response")
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterName;
    
    private Long reportedUserId;
    private String reportedUserName;
    
    private Long matchId;
    
    private String title;
    private String description;
    private String category;
    private String status;
    private String evidenceUrl;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
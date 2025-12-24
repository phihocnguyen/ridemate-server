package com.ridemate.ridemate_server.application.service.report;

import com.ridemate.ridemate_server.application.dto.report.*;
import com.ridemate.ridemate_server.domain.entity.Report;

import java.util.List;

public interface ReportManagementService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final com.ridemate.ridemate_server.domain.repository.SessionRepository sessionRepository;
    private final com.ridemate.ridemate_server.domain.repository.MessageRepository messageRepository;

    /**
     * Get all reports with pagination and filters
     */
    public ReportManagementPageDto getAllReports(
    ReportManagementPageDto getAllReports(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    ReportStatisticsDto getReportStatistics();

    List<ReportManagementDto> getPendingReports();

    ReportManagementDto getAdminReportDetail(Long id);

    ReportManagementDto processReport(Long id, String adminNotes);

    ReportManagementDto resolveReport(Long id, ReportActionRequest request, String adminUsername);

    /**
     * Map Report entity to ReportManagementDto
     */
    private ReportManagementDto mapToReportManagementDto(Report report) {
        // Fetch session messages if match exists
        List<SessionMessageDto> sessionMessages = null;
        if (report.getMatch() != null) {
            sessionMessages = fetchSessionMessages(report.getMatch().getId());
        }
        
        return ReportManagementDto.builder()
            .id(report.getId())
            .reporterId(report.getReporter().getId())
            .reporterName(report.getReporter().getFullName())
            .reporterPhone(report.getReporter().getPhoneNumber())
            .reportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null)
            .reportedUserName(report.getReportedUser() != null ? report.getReportedUser().getFullName() : null)
            .reportedUserPhone(report.getReportedUser() != null ? report.getReportedUser().getPhoneNumber() : null)
            .matchId(report.getMatch() != null ? report.getMatch().getId() : null)
            .title(report.getTitle())
            .description(report.getDescription())
            .category(report.getCategory().name())
            .status(report.getStatus().name())
            .evidenceUrl(report.getEvidenceUrl())
            .createdAt(report.getCreatedAt())
            .updatedAt(report.getUpdatedAt())
            .sessionMessages(sessionMessages)
            .build();
    }
    
    /**
     * Fetch all messages from a session for admin review
     */
    private List<SessionMessageDto> fetchSessionMessages(Long matchId) {
        try {
            return sessionRepository.findByMatchId(matchId)
                .map(session -> {
                    List<com.ridemate.ridemate_server.domain.entity.Message> messages = 
                        messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
                    
                    return messages.stream()
                        .map(msg -> SessionMessageDto.builder()
                            .id(msg.getId())
                            .senderId(msg.getSender().getId())
                            .senderName(msg.getSender().getFullName())
                            .content(msg.getContent())
                            .type(msg.getType().name())
                            .createdAt(msg.getCreatedAt())
                            .build())
                        .collect(Collectors.toList());
                })
                .orElse(List.of()); // Return empty list if no session found
        } catch (Exception e) {
            log.warn("Failed to fetch session messages for match {}: {}", matchId, e.getMessage());
            return List.of();
        }
    }
}
    ReportManagementDto rejectReport(Long id, String reason, String adminUsername);
}

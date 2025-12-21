package com.ridemate.ridemate_server.application.service.report.impl;

import com.ridemate.ridemate_server.application.dto.report.*;
import com.ridemate.ridemate_server.application.service.report.ReportManagementService;
import com.ridemate.ridemate_server.application.service.report.ReportService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.ReportRepository;
import com.ridemate.ridemate_server.domain.repository.ReportSpecification;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService, ReportManagementService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    // =================================================================
    // PHẦN 1: USER APP
    // =================================================================

    @Override
    @Transactional
    public ReportResponse createReport(Long reporterId, CreateReportRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("Reporter not found"));

        User reportedUser = null;
        if (request.getReportedUserId() != null) {
            reportedUser = userRepository.findById(request.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reported user not found"));
        }

        Match match = null;
        if (request.getMatchId() != null) {
            match = matchRepository.findById(request.getMatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        }

        // FIX LỖI 1: Không gọi .createdAt() trong builder (vì Lombok Builder không thấy field của lớp cha)
        Report report = Report.builder()
                .reporter(reporter)
                .reportedUser(reportedUser)
                .match(match)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(Report.ReportCategory.valueOf(request.getCategory()))
                .evidenceUrl(request.getEvidenceUrl())
                .status(Report.ReportStatus.PENDING)
                .build();

        report.setCreatedAt(LocalDateTime.now());

        report = reportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    public List<ReportResponse> getMyReports(Long reporterId) {
        return reportRepository.findByReporterId(reporterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        return mapToResponse(report);
    }

    // PHẦN 2: ADMIN DASHBOARD

    @Override
    public ReportManagementPageDto getAllReports(Report.ReportStatus status, Report.ReportCategory category, String searchTerm, int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Report> reportPage = reportRepository.findAll(
                ReportSpecification.getReportsByFilter(status, category, searchTerm),
                pageable
        );

        List<ReportManagementDto> reportDtos = reportPage.getContent().stream()
                .map(this::mapToManagementDto)
                .collect(Collectors.toList());

        return ReportManagementPageDto.builder()
                .reports(reportDtos)
                .pageNo(reportPage.getNumber())
                .pageSize(reportPage.getSize())
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .last(reportPage.isLast())
                .build();
    }

    @Override
    public ReportStatisticsDto getReportStatistics() {
        return ReportStatisticsDto.builder()
                .totalReports(reportRepository.count())
                .pendingReports(reportRepository.countByStatus(Report.ReportStatus.PENDING))
                .processingReports(reportRepository.countByStatus(Report.ReportStatus.PROCESSING))
                .resolvedReports(reportRepository.countByStatus(Report.ReportStatus.RESOLVED))
                .rejectedReports(reportRepository.countByStatus(Report.ReportStatus.REJECTED))
                .build();
    }

    @Override
    public List<ReportManagementDto> getPendingReports() {
        return reportRepository.findByStatus(Report.ReportStatus.PENDING).stream()
                .map(this::mapToManagementDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReportManagementDto getAdminReportDetail(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        return mapToManagementDto(report);
    }

    @Override
    @Transactional
    public ReportManagementDto processReport(Long id, String adminNotes) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        report.setStatus(Report.ReportStatus.PROCESSING);
        if (adminNotes != null) report.setResolutionNotes(adminNotes);
        
        return mapToManagementDto(reportRepository.save(report));
    }

    @Override
    @Transactional
    public ReportManagementDto resolveReport(Long id, ReportActionRequest request, String adminUsername) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        report.setStatus(Report.ReportStatus.RESOLVED);
        
        if (request.getActionType() != null) {
            try {
                Report.ResolutionAction action = Report.ResolutionAction.valueOf(request.getActionType().name());
                report.setResolutionAction(action); 
            } catch (IllegalArgumentException e) {
                log.error("Invalid resolution action: " + request.getActionType());
            }
        }
        
        report.setResolutionNotes(request.getReason());
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername);
        
        return mapToManagementDto(reportRepository.save(report));
    }

    @Override
    @Transactional
    public ReportManagementDto rejectReport(Long id, String reason, String adminUsername) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        
        report.setStatus(Report.ReportStatus.REJECTED);
        report.setResolutionNotes(reason);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminUsername);
        
        return mapToManagementDto(reportRepository.save(report));
    }



    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .category(report.getCategory().name())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private ReportManagementDto mapToManagementDto(Report report) {
        ReportManagementDto.UserInfo reporterInfo = null;
        String reporterName = "Unknown"; 
        
        if (report.getReporter() != null) {
            User r = report.getReporter();
            reporterName = r.getFullName();
            reporterInfo = ReportManagementDto.UserInfo.builder()
                    .id(r.getId())
                    .fullName(r.getFullName())
                    .phoneNumber(r.getPhoneNumber())
                    .profilePictureUrl(r.getProfilePictureUrl())
                    .build();
        }

        ReportManagementDto.UserInfo reportedUserInfo = null;
        if (report.getReportedUser() != null) {
            User ru = report.getReportedUser();
            reportedUserInfo = ReportManagementDto.UserInfo.builder()
                    .id(ru.getId())
                    .fullName(ru.getFullName())
                    .phoneNumber(ru.getPhoneNumber())
                    .profilePictureUrl(ru.getProfilePictureUrl())
                    .build();
        }

        return ReportManagementDto.builder()
                .id(report.getId())
                .reporter(reporterInfo)
                .reporterName(reporterName)
                .reportedUser(reportedUserInfo)
                .matchId(report.getMatch() != null ? report.getMatch().getId() : null)
                .title(report.getTitle())
                .description(report.getDescription())
                .category(report.getCategory() != null ? report.getCategory().name() : null)
                .status(report.getStatus() != null ? report.getStatus().name() : null)
                .evidenceUrl(report.getEvidenceUrl())
                .resolutionAction(report.getResolutionAction() != null ? report.getResolutionAction().name() : null)
                .resolutionNotes(report.getResolutionNotes())
                .resolvedAt(report.getResolvedAt())
                .resolvedBy(report.getResolvedBy())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
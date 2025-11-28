package com.ridemate.ridemate_server.application.service.report.impl;

import com.ridemate.ridemate_server.application.dto.report.CreateReportRequest;
import com.ridemate.ridemate_server.application.dto.report.ReportResponse;
import com.ridemate.ridemate_server.application.mapper.ReportMapper;
import com.ridemate.ridemate_server.application.service.report.ReportService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.ReportRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private ReportMapper reportMapper;

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

        report = reportRepository.save(report);
        return reportMapper.toResponse(report);
    }

    @Override
    public List<ReportResponse> getMyReports(Long reporterId) {
        List<Report> reports = reportRepository.findByReporterId(reporterId);
        return reports.stream()
                .map(reportMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReportResponse getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        return reportMapper.toResponse(report);
    }
}
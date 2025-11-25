package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.service.admin.AdminService;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import com.ridemate.ridemate_server.domain.repository.VoucherRepository;
import com.ridemate.ridemate_server.presentation.dto.admin.AdminChartDataDto;
import com.ridemate.ridemate_server.presentation.dto.admin.AdminDashboardStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final VoucherRepository voucherRepository;
    private final VehicleRepository vehicleRepository;
    private final com.ridemate.ridemate_server.domain.repository.MessageRepository messageRepository;
    private final com.ridemate.ridemate_server.domain.repository.UserVoucherRepository userVoucherRepository;
    private final com.ridemate.ridemate_server.domain.repository.MatchRepository matchRepository;
    private final com.ridemate.ridemate_server.domain.repository.FeedbackRepository feedbackRepository;

    @Override
    public AdminDashboardStatsDto getDashboardStats() {
        return AdminDashboardStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalSessions(sessionRepository.count())
                .totalVouchers(voucherRepository.count())
                .totalVehicles(vehicleRepository.count())
                .totalCompletedTrips(matchRepository.countByStatus(com.ridemate.ridemate_server.domain.entity.Match.MatchStatus.COMPLETED))
                .totalCancelledTrips(matchRepository.countByStatus(com.ridemate.ridemate_server.domain.entity.Match.MatchStatus.CANCELLED))
                .totalReports(feedbackRepository.count())
                .build();
    }

    @Override
    public AdminChartDataDto getChartData(String type) {
        if ("users".equalsIgnoreCase(type)) {
            return getUserChartData();
        } else if ("sessions".equalsIgnoreCase(type)) {
            return getSessionChartData();
        } else if ("messages".equalsIgnoreCase(type)) {
            return getMessageChartData();
        } else if ("vouchers".equalsIgnoreCase(type)) {
            return getVoucherChartData();
        } else if ("revenue".equalsIgnoreCase(type)) {
            return getRevenueChartData();
        }
        return new AdminChartDataDto();
    }

    private AdminChartDataDto getUserChartData() {
        List<User> users = userRepository.findAll();
        Map<String, Long> groupedByDate = users.stream()
                .collect(Collectors.groupingBy(
                        user -> user.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
        
        return prepareChartData(groupedByDate, "New Users");
    }

    private AdminChartDataDto getSessionChartData() {
        List<Session> sessions = sessionRepository.findAll();
        Map<String, Long> groupedByDate = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Sessions");
    }

    private AdminChartDataDto getMessageChartData() {
        List<com.ridemate.ridemate_server.domain.entity.Message> messages = messageRepository.findAll();
        Map<String, Long> groupedByDate = messages.stream()
                .collect(Collectors.groupingBy(
                        message -> message.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Messages");
    }

    private AdminChartDataDto getVoucherChartData() {
        List<com.ridemate.ridemate_server.domain.entity.UserVoucher> userVouchers = userVoucherRepository.findAll();
        Map<String, Long> groupedByDate = userVouchers.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        return prepareChartData(groupedByDate, "Vouchers Redeemed");
    }

    private AdminChartDataDto getRevenueChartData() {
        List<com.ridemate.ridemate_server.domain.entity.UserVoucher> userVouchers = userVoucherRepository.findAll();
        Map<String, Long> groupedByDate = userVouchers.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getCreatedAt().toLocalDate().toString(),
                        Collectors.summingLong(uv -> uv.getVoucher().getCost())
                ));

        return prepareChartData(groupedByDate, "Revenue (Coins)");
    }

    private AdminChartDataDto prepareChartData(Map<String, Long> groupedByDate, String label) {
        List<String> sortedDates = new ArrayList<>(groupedByDate.keySet());
        Collections.sort(sortedDates);

        List<Long> data = sortedDates.stream()
                .map(groupedByDate::get)
                .collect(Collectors.toList());

        return AdminChartDataDto.builder()
                .labels(sortedDates)
                .data(data)
                .label(label)
                .build();
    }
}

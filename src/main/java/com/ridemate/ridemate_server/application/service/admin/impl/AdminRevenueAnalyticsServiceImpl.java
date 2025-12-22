package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.service.admin.AdminRevenueAnalyticsService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminRevenueAnalyticsServiceImpl implements AdminRevenueAnalyticsService {

    private final MatchRepository matchRepository;

    @Override
    public Long getTotalRevenue() {
        return matchRepository.findByStatus(Match.MatchStatus.COMPLETED).stream()
                .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                .sum();
    }

    @Override
    public Long getTodayRevenue() {
        LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return matchRepository.findByStatus(Match.MatchStatus.COMPLETED).stream()
                .filter(m -> m.getCreatedAt().isAfter(startOfToday))
                .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                .sum();
    }

    @Override
    public Long getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return matchRepository.findByStatus(Match.MatchStatus.COMPLETED).stream()
                .filter(m -> {
                    LocalDateTime createdAt = m.getCreatedAt();
                    return !createdAt.isBefore(startDateTime) && !createdAt.isAfter(endDateTime);
                })
                .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                .sum();
    }

    @Override
    public Double getRevenueGrowthPercentage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfThisMonth = now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        
        List<Match> completedMatches = matchRepository.findByStatus(Match.MatchStatus.COMPLETED);
        
        long thisMonthRevenue = completedMatches.stream()
                .filter(m -> m.getCreatedAt().isAfter(startOfThisMonth))
                .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                .sum();
        
        long lastMonthRevenue = completedMatches.stream()
                .filter(m -> {
                    LocalDateTime createdAt = m.getCreatedAt();
                    return !createdAt.isBefore(startOfLastMonth) && createdAt.isBefore(startOfThisMonth);
                })
                .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                .sum();
        
        if (lastMonthRevenue == 0) return 0.0;
        
        return ((thisMonthRevenue - lastMonthRevenue) * 100.0) / lastMonthRevenue;
    }
}

package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.TripVolumeResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminTripAnalyticsService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTripAnalyticsServiceImpl implements AdminTripAnalyticsService {

    private final MatchRepository matchRepository;

    @Override
    public List<TripVolumeResponse> getTripVolumeByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Match> matches = matchRepository.findAll();
        
        Map<LocalDate, List<Match>> groupedByDate = matches.stream()
                .filter(m -> {
                    LocalDate matchDate = m.getCreatedAt().toLocalDate();
                    return !matchDate.isBefore(startDate) && !matchDate.isAfter(endDate);
                })
                .collect(Collectors.groupingBy(m -> m.getCreatedAt().toLocalDate()));
        
        return groupedByDate.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Match> dayMatches = entry.getValue();
                    
                    long completedTrips = dayMatches.stream()
                            .filter(m -> m.getStatus() == Match.MatchStatus.COMPLETED)
                            .count();
                    
                    long cancelledTrips = dayMatches.stream()
                            .filter(m -> m.getStatus() == Match.MatchStatus.CANCELLED)
                            .count();
                    
                    long totalCoin = dayMatches.stream()
                            .filter(m -> m.getStatus() == Match.MatchStatus.COMPLETED)
                            .mapToLong(m -> m.getCoin() != null ? m.getCoin() : 0)
                            .sum();
                    
                    return TripVolumeResponse.builder()
                            .date(date)
                            .tripCount((long) dayMatches.size())
                            .totalCoin(totalCoin)
                            .completedTrips(completedTrips)
                            .cancelledTrips(cancelledTrips)
                            .build();
                })
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public Long getTotalTrips() {
        return matchRepository.count();
    }

    @Override
    public Long getTodayTrips() {
        LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return matchRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().isAfter(startOfToday))
                .count();
    }

    @Override
    public Long getCompletedTripsCount() {
        return matchRepository.countByStatus(Match.MatchStatus.COMPLETED);
    }

    @Override
    public Long getCancelledTripsCount() {
        return matchRepository.countByStatus(Match.MatchStatus.CANCELLED);
    }
}

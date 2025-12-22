package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.DriverRankingResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminDriverAnalyticsService;
import com.ridemate.ridemate_server.domain.entity.Feedback;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.FeedbackRepository;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDriverAnalyticsServiceImpl implements AdminDriverAnalyticsService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public List<DriverRankingResponse> getTopDrivers(int limit) {
        List<Match> completedMatches = matchRepository.findByStatus(Match.MatchStatus.COMPLETED);
        
        Map<Long, List<Match>> driverMatches = completedMatches.stream()
                .filter(m -> m.getDriver() != null)
                .collect(Collectors.groupingBy(m -> m.getDriver().getId()));
        
        List<DriverRankingResponse> rankings = driverMatches.entrySet().stream()
                .map(entry -> {
                    Long driverId = entry.getKey();
                    List<Match> matches = entry.getValue();
                    
                    User driver = userRepository.findById(driverId).orElse(null);
                    if (driver == null) return null;
                    
                    int totalTrips = matches.size();
                    int totalCoins = matches.stream()
                            .mapToInt(m -> m.getCoin() != null ? m.getCoin() : 0)
                            .sum();
                    
                    List<Feedback> driverFeedbacks = feedbackRepository.findAll().stream()
                            .filter(f -> f.getReviewed() != null && f.getReviewed().getId().equals(driverId))
                            .collect(Collectors.toList());
                    
                    double avgRating = driverFeedbacks.stream()
                            .mapToDouble(f -> f.getRating() != null ? f.getRating() : 0)
                            .average()
                            .orElse(0.0);
                    
                    List<Match> allDriverMatches = matchRepository.findByDriverId(driverId);
                    long acceptedMatches = allDriverMatches.stream()
                            .filter(m -> m.getStatus() != Match.MatchStatus.PENDING && 
                                         m.getStatus() != Match.MatchStatus.WAITING)
                            .count();
                    
                    double acceptanceRate = allDriverMatches.size() > 0 
                            ? (acceptedMatches * 100.0 / allDriverMatches.size()) : 0;
                    double completionRate = acceptedMatches > 0 
                            ? (totalTrips * 100.0 / acceptedMatches) : 0;
                    
                    return DriverRankingResponse.builder()
                            .driverId(driverId)
                            .driverName(driver.getFullName())
                            .driverPhone(driver.getPhoneNumber())
                            .avatarUrl(driver.getProfilePictureUrl())
                            .totalTrips(totalTrips)
                            .totalCoinsEarned(totalCoins)
                            .averageRating(avgRating)
                            .acceptanceRate(acceptanceRate)
                            .completionRate(completionRate)
                            .build();
                })
                .filter(r -> r != null)
                .sorted((a, b) -> Integer.compare(b.getTotalCoinsEarned(), a.getTotalCoinsEarned()))
                .limit(limit)
                .collect(Collectors.toList());
        
        int rank = 1;
        for (DriverRankingResponse ranking : rankings) {
            ranking.setRank(rank++);
        }
        
        return rankings;
    }

    @Override
    public Long getTotalDrivers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getUserType() == User.UserType.DRIVER)
                .count();
    }

    @Override
    public Long getActiveDrivers() {
        return userRepository.findByUserTypeAndDriverStatus(
                User.UserType.DRIVER, 
                User.DriverStatus.ONLINE
        ).stream().count();
    }

    @Override
    public Double getAverageDriverRating() {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        return allFeedbacks.stream()
                .filter(f -> f.getRating() != null)
                .mapToDouble(Feedback::getRating)
                .average()
                .orElse(0.0);
    }
}

package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.TripDetailResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminTripDetailService;
import com.ridemate.ridemate_server.domain.entity.Feedback;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.FeedbackRepository;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTripDetailServiceImpl implements AdminTripDetailService {

    private final MatchRepository matchRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    public TripDetailResponse getTripDetail(Long tripId) {
        Match match = matchRepository.findById(tripId).orElse(null);
        if (match == null) return null;
        
        return convertToTripDetailResponse(match);
    }

    @Override
    public List<TripDetailResponse> getAllTripsDetailed() {
        return matchRepository.findAll().stream()
                .map(this::convertToTripDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDetailResponse> getCompletedTripsDetailed() {
        return matchRepository.findByStatus(Match.MatchStatus.COMPLETED).stream()
                .map(this::convertToTripDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDetailResponse> getCancelledTripsDetailed() {
        return matchRepository.findByStatus(Match.MatchStatus.CANCELLED).stream()
                .map(this::convertToTripDetailResponse)
                .collect(Collectors.toList());
    }

    private TripDetailResponse convertToTripDetailResponse(Match match) {
        User driver = match.getDriver();
        User passenger = match.getPassenger();
        
        Double driverRating = null;
        if (driver != null) {
            List<Feedback> driverFeedbacks = feedbackRepository.findAll().stream()
                    .filter(f -> f.getReviewed() != null && f.getReviewed().getId().equals(driver.getId()))
                    .collect(Collectors.toList());
            
            if (!driverFeedbacks.isEmpty()) {
                driverRating = driverFeedbacks.stream()
                        .mapToDouble(f -> f.getRating() != null ? f.getRating() : 0)
                        .average()
                        .orElse(0.0);
            }
        }
        
        return TripDetailResponse.builder()
                .tripId(match.getId())
                .tripCode("RID-" + match.getId())
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getFullName() : null)
                .driverPhone(driver != null ? driver.getPhoneNumber() : null)
                .driverAvatar(driver != null ? driver.getProfilePictureUrl() : null)
                .driverRating(driverRating)
                .passengerId(passenger.getId())
                .passengerName(passenger.getFullName())
                .passengerPhone(passenger.getPhoneNumber())
                .passengerAvatar(passenger.getProfilePictureUrl())
                .pickupAddress(match.getPickupAddress())
                .pickupLatitude(match.getPickupLatitude())
                .pickupLongitude(match.getPickupLongitude())
                .dropoffAddress(match.getDestinationAddress())
                .dropoffLatitude(match.getDestinationLatitude())
                .dropoffLongitude(match.getDestinationLongitude())
                .coinAmount(match.getCoin())
                .distance(calculateDistance(match))
                .duration(30)
                .status(match.getStatus().name())
                .createdAt(match.getCreatedAt())
                .acceptedAt(match.getUpdatedAt())
                .startedAt(match.getStartTime())
                .completedAt(match.getEndTime())
                .build();
    }

    private Double calculateDistance(Match match) {
        if (match.getPickupLatitude() == null || match.getPickupLongitude() == null ||
            match.getDestinationLatitude() == null || match.getDestinationLongitude() == null) {
            return null;
        }
        
        double lat1 = match.getPickupLatitude();
        double lon1 = match.getPickupLongitude();
        double lat2 = match.getDestinationLatitude();
        double lon2 = match.getDestinationLongitude();
        
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}

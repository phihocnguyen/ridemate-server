package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.ActiveTripResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminRealTimeMonitoringService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRealTimeMonitoringServiceImpl implements AdminRealTimeMonitoringService {

    private final MatchRepository matchRepository;

    @Override
    public List<ActiveTripResponse> getActiveTrips() {
        List<Match> activeMatches = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == Match.MatchStatus.ACCEPTED || 
                             m.getStatus() == Match.MatchStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        
        return convertToActiveTripResponses(activeMatches);
    }

    @Override
    public Long getActiveTripsCount() {
        return matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == Match.MatchStatus.ACCEPTED || 
                             m.getStatus() == Match.MatchStatus.IN_PROGRESS)
                .count();
    }

    @Override
    public List<ActiveTripResponse> getTripsInProgress() {
        List<Match> inProgressMatches = matchRepository.findByStatus(Match.MatchStatus.IN_PROGRESS);
        return convertToActiveTripResponses(inProgressMatches);
    }

    @Override
    public List<ActiveTripResponse> getAcceptedTrips() {
        List<Match> acceptedMatches = matchRepository.findByStatus(Match.MatchStatus.ACCEPTED);
        return convertToActiveTripResponses(acceptedMatches);
    }

    private List<ActiveTripResponse> convertToActiveTripResponses(List<Match> matches) {
        return matches.stream()
                .map(match -> {
                    User driver = match.getDriver();
                    User passenger = match.getPassenger();
                    
                    String vehicleInfo = null;
                    if (match.getVehicle() != null) {
                        vehicleInfo = match.getVehicle().getVehicleType() + " - " + 
                                      match.getVehicle().getLicensePlate();
                    }
                    
                    return ActiveTripResponse.builder()
                            .matchId(match.getId())
                            .status(match.getStatus().name())
                            .driverId(driver != null ? driver.getId() : null)
                            .driverName(driver != null ? driver.getFullName() : null)
                            .driverPhone(driver != null ? driver.getPhoneNumber() : null)
                            .vehicleInfo(vehicleInfo)
                            .passengerId(passenger.getId())
                            .passengerName(passenger.getFullName())
                            .passengerPhone(passenger.getPhoneNumber())
                            .pickupAddress(match.getPickupAddress())
                            .destinationAddress(match.getDestinationAddress())
                            .coin(match.getCoin())
                            .startTime(match.getCreatedAt())
                            .acceptedAt(match.getUpdatedAt())
                            .estimatedDuration(30)
                            .currentLatitude(match.getPickupLatitude())
                            .currentLongitude(match.getPickupLongitude())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

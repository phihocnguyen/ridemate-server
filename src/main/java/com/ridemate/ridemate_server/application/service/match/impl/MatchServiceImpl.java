package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.mapper.MatchMapper;
import com.ridemate.ridemate_server.application.service.match.MatchService;
import com.ridemate.ridemate_server.application.service.session.SessionService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Vehicle; 
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository; 
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MatchMapper matchMapper;

    @Autowired
    private SessionService sessionService;

    @Override
    @Transactional
    public MatchResponse bookRide(Long passengerId, BookRideRequest request) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (passenger.getUserType() == User.UserType.DRIVER) {
             
        }

        Match match = Match.builder()
                .passenger(passenger)
                .pickupAddress(request.getPickupAddress())
                .destinationAddress(request.getDestinationAddress())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .fare(request.getFare())
                .status(Match.MatchStatus.WAITING)
                .build();

        match = matchRepository.save(match);
        
        sessionService.createSession(match);
        
        
        return matchMapper.toResponse(match);
    }

    @Override
    public MatchResponse getMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        return matchMapper.toResponse(match);
    }

    @Override
    public List<MatchResponse> getMyHistory(Long userId) {
        List<Match> matches = matchRepository.findByPassengerId(userId);
        return matches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }

@Override
    @Transactional
    public MatchResponse acceptRide(Long matchId, Long driverId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != Match.MatchStatus.WAITING) {
            throw new IllegalArgumentException("Match is no longer available (Status: " + match.getStatus() + ")");
        }

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driver.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("Only users with DRIVER role can accept rides");
        }

        List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndStatus(driverId, Vehicle.VehicleStatus.APPROVED);
        if (vehicles.isEmpty()) {
            throw new IllegalArgumentException("Driver does not have an active (APPROVED) vehicle");
        }
        
        Vehicle vehicle = vehicles.get(0);

        match.setDriver(driver);
        match.setVehicle(vehicle);
        match.setStatus(Match.MatchStatus.ACCEPTED);

        match = matchRepository.save(match);

        return matchMapper.toResponse(match);
    }
}
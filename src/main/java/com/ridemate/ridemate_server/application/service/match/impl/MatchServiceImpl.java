package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.dto.match.UpdateMatchStatusRequest;
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
        List<Match> driverMatches = matchRepository.findByDriverId(userId);
        matches.addAll(driverMatches);
        
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
            throw new IllegalArgumentException("Match is no longer available");
        }

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driver.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("Only users with DRIVER role can accept rides");
        }

        List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndStatus(driverId, Vehicle.VehicleStatus.APPROVED);
        if (vehicles.isEmpty()) {
            throw new IllegalArgumentException("Driver does not have an active vehicle");
        }
        
        Vehicle vehicle = vehicles.get(0);

        match.setDriver(driver);
        match.setVehicle(vehicle);
        match.setStatus(Match.MatchStatus.ACCEPTED);

        match = matchRepository.save(match);

        return matchMapper.toResponse(match);
    }

    @Override
    @Transactional
    public MatchResponse updateMatchStatus(Long matchId, Long userId, UpdateMatchStatusRequest request) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        boolean isDriver = match.getDriver() != null && match.getDriver().getId().equals(userId);
        boolean isPassenger = match.getPassenger().getId().equals(userId);

        if (!isDriver && !isPassenger) {
             throw new IllegalArgumentException("You are not authorized to update this match");
        }

        Match.MatchStatus newStatus;
        try {
            newStatus = Match.MatchStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
        }

        if (isPassenger && newStatus != Match.MatchStatus.CANCELLED) {
            throw new IllegalArgumentException("Passenger can only CANCEL the ride");
        }

        match.setStatus(newStatus);
        match = matchRepository.save(match);

        if (newStatus == Match.MatchStatus.COMPLETED || newStatus == Match.MatchStatus.CANCELLED) {
            try {
                sessionService.endSession(matchId);
            } catch (Exception e) {
            }
        }

        return matchMapper.toResponse(match);
    }

    @Override
    @Transactional
    public MatchResponse cancelMatch(Long matchId, Long userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        // Kiểm tra quyền: Phải là Tài xế hoặc Hành khách của cuốc này
        boolean isPassenger = match.getPassenger().getId().equals(userId);
        boolean isDriver = match.getDriver() != null && match.getDriver().getId().equals(userId);

        if (!isPassenger && !isDriver) {
            throw new IllegalArgumentException("You are not authorized to cancel this match");
        }

        // Không được hủy nếu đã hoàn thành hoặc đã hủy
        if (match.getStatus() == Match.MatchStatus.COMPLETED || match.getStatus() == Match.MatchStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot cancel a completed or already cancelled match");
        }

        match.setStatus(Match.MatchStatus.CANCELLED);
        match = matchRepository.save(match);

        // Kết thúc session ngay lập tức
        try {
            sessionService.endSession(matchId);
        } catch (Exception e) {
            // Bỏ qua nếu session đã đóng
        }

        return matchMapper.toResponse(match);
    }
}
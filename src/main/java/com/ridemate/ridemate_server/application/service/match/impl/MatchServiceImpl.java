package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.dto.match.UpdateMatchStatusRequest;
import com.ridemate.ridemate_server.application.mapper.MatchMapper;
import com.ridemate.ridemate_server.application.service.match.CoinCalculationService;
import com.ridemate.ridemate_server.application.service.match.DriverMatchingService;
import com.ridemate.ridemate_server.application.service.match.MatchService;
import com.ridemate.ridemate_server.application.service.notification.NotificationService;
import com.ridemate.ridemate_server.application.service.session.SessionService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Vehicle;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Autowired
    private DriverMatchingService driverMatchingService;

    @Autowired
    private CoinCalculationService coinCalculationService;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public MatchResponse bookRide(Long passengerId, BookRideRequest request) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate pickup location is provided
        if (request.getPickupLatitude() == null || request.getPickupLongitude() == null) {
            throw new IllegalArgumentException("Pickup location (latitude, longitude) is required");
        }

        // Calculate coin based on distance
        Integer coin = null;
        if (request.getDestinationLatitude() != null && request.getDestinationLongitude() != null) {
            coin = coinCalculationService.calculateCoinFromCoordinates(
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    request.getDestinationLatitude(), request.getDestinationLongitude()
            );
            log.info("Calculated coin: {} for route ({}, {}) → ({}, {})",
                    coin,
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    request.getDestinationLatitude(), request.getDestinationLongitude());
        }

        // Create match with PENDING status initially
        Match match = Match.builder()
                .passenger(passenger)
                .pickupAddress(request.getPickupAddress())
                .destinationAddress(request.getDestinationAddress())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .coin(coin)
                .status(Match.MatchStatus.PENDING)  // Start as PENDING
                .build();

        match = matchRepository.save(match);
        log.info("Match {} created with status PENDING for passenger {}", match.getId(), passengerId);
        
        sessionService.createSession(match);

        // ===== AUTOMATIC DRIVER MATCHING =====
        log.info("Finding best drivers for match {} at pickup location ({}, {})", 
                match.getId(), request.getPickupLatitude(), request.getPickupLongitude());
        
        List<DriverCandidate> candidates = driverMatchingService.findBestDrivers(match);
        
        if (candidates.isEmpty()) {
            log.warn("No available drivers found for match {} - Status remains PENDING", match.getId());
            // Match stays in PENDING status - waiting for drivers to come online
            
        } else {
            // Update match status to WAITING (có driver available)
            match.setStatus(Match.MatchStatus.WAITING);
            match = matchRepository.save(match);
            
            log.info("Found {} driver candidates for match {}. Top candidate: Driver {} (score: {:.3f})",
                    candidates.size(), match.getId(), 
                    candidates.get(0).getDriverId(),
                    candidates.get(0).getMatchScore());
            log.info("Match {} updated to WAITING status", match.getId());
            
            // Log all candidates for debugging
            for (int i = 0; i < candidates.size(); i++) {
                DriverCandidate dc = candidates.get(i);
                log.info("  Rank #{}: Driver {} - Distance: {:.2f}km, ETA: {}min, Score: {:.3f}",
                        i + 1, dc.getDriverId(), dc.getDistanceToPickup(), 
                        dc.getEstimatedArrivalTime(), dc.getMatchScore());
            }
            
            // ===== SEND NOTIFICATIONS TO DRIVERS =====
            log.info("Sending notifications to {} drivers for match {}", candidates.size(), match.getId());
            notificationService.notifyDriversOfNewMatch(match, candidates);
            
            // TODO: In next phase, implement:
            // 1. Send push notifications to top 3-5 drivers
            // 2. Set up timeout mechanism (15-30 seconds per driver)
            // 3. Handle driver accept/reject responses
            // 4. Fallback to next driver if timeout or reject
        }
        
        // Build response with candidates
        MatchResponse response = matchMapper.toResponse(match);
        response.setMatchedDriverCandidates(candidates);
        response.setMessage(candidates.isEmpty() ? 
            "No drivers available at the moment. Your request is queued." : 
            null);
        
        return response;
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

        // ===== UPDATE DRIVER METRICS =====
        driver.setTotalRidesAccepted(driver.getTotalRidesAccepted() + 1);
        driver.setDriverStatus(User.DriverStatus.BUSY); // Driver is now busy
        userRepository.save(driver);

        log.info("Driver {} accepted match {}. Total rides accepted: {}", 
                driverId, matchId, driver.getTotalRidesAccepted());

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

        // ===== UPDATE DRIVER METRICS WHEN COMPLETED =====
        if (newStatus == Match.MatchStatus.COMPLETED && match.getDriver() != null) {
            User driver = match.getDriver();
            driver.setTotalRidesCompleted(driver.getTotalRidesCompleted() + 1);
            
            // Recalculate completion rate
            if (driver.getTotalRidesAccepted() > 0) {
                float completionRate = ((float) driver.getTotalRidesCompleted() / driver.getTotalRidesAccepted()) * 100;
                driver.setCompletionRate(completionRate);
            }
            
            // Set driver back to ONLINE (available for next ride)
            driver.setDriverStatus(User.DriverStatus.ONLINE);
            
            userRepository.save(driver);
            
            log.info("Driver {} completed match {}. Stats - Completed: {}, Completion Rate: {:.1f}%", 
                    driver.getId(), matchId, driver.getTotalRidesCompleted(), driver.getCompletionRate());
        }

        // ===== SET DRIVER BACK TO ONLINE IF CANCELLED =====
        if (newStatus == Match.MatchStatus.CANCELLED && match.getDriver() != null) {
            User driver = match.getDriver();
            driver.setDriverStatus(User.DriverStatus.ONLINE);
            userRepository.save(driver);
        }

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
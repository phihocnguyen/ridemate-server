package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.BroadcastDriverRequest;
import com.ridemate.ridemate_server.application.dto.match.BroadcastPassengerRequest;
import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.application.dto.match.UpdateMatchStatusRequest;
import com.ridemate.ridemate_server.application.mapper.MatchMapper;
import com.ridemate.ridemate_server.application.dto.match.FindMatchesRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.dto.user.UserDto;
import com.ridemate.ridemate_server.application.mapper.UserMapper;
import com.ridemate.ridemate_server.application.service.user.UserService;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchServiceImpl implements MatchService {

    // Temporary storage for broadcasts (in production, use Redis or database)
    private static final List<BroadcastDriverRequest> driverBroadcasts = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final List<BroadcastPassengerRequest> passengerBroadcasts = new java.util.concurrent.CopyOnWriteArrayList<>();
    private static final java.util.Map<Long, BroadcastDriverRequest> driverBroadcastMap = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Long, BroadcastPassengerRequest> passengerBroadcastMap = new java.util.concurrent.ConcurrentHashMap<>();

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

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

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
                .fare(coin != null ? coin : 0)
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
            
            // Save match first WITHOUT candidates to avoid type casting issues
            match = matchRepository.save(match);
            
            // ===== SAVE CANDIDATES TO DATABASE FOR SUPABASE REALTIME =====
            // Use native query with explicit JSONB cast to avoid Hibernate type issues
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String candidatesJson = objectMapper.writeValueAsString(candidates);
                
                // Update using native SQL with JSONB cast
                matchRepository.updateMatchedDriverCandidates(match.getId(), candidatesJson);
                
                log.info("Serialized {} candidates to JSON for match {}", candidates.size(), match.getId());
            } catch (Exception e) {
                log.error("Failed to serialize candidates to JSON: {}", e.getMessage());
                // Continue even if serialization fails
            }
            
            
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
        
        MatchResponse response = matchMapper.toResponse(match);
        
        // ✅ If match has a driver, get CURRENT driver location from User entity
        if (match.getDriver() != null) {
            User driver = match.getDriver();
            Vehicle vehicle = match.getVehicle();
            
            if (driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null) {
                DriverCandidate currentDriverInfo = DriverCandidate.builder()
                        .driverId(driver.getId())
                        .driverName(driver.getFullName())
                        .driverPhone(driver.getPhoneNumber())
                        .vehicleId(vehicle != null ? vehicle.getId() : null)
                        .vehicleInfo(vehicle != null ? 
                            vehicle.getMake() + " " + vehicle.getModel() + " - " + vehicle.getLicensePlate() : null)
                        .currentLatitude(driver.getCurrentLatitude())
                        .currentLongitude(driver.getCurrentLongitude())
                        .driverRating(driver.getRating())
                        .build();
                
                response.setMatchedDriverCandidates(List.of(currentDriverInfo));
                
                log.debug("✅ Loaded current driver location for match {}: ({}, {})", 
                        matchId, driver.getCurrentLatitude(), driver.getCurrentLongitude());
            } else {
                log.debug("⚠️ Driver {} for match {} has no current location", 
                        driver.getId(), matchId);
            }
        } else if (match.getMatchedDriverCandidates() != null) {
            // Fallback: Parse matchedDriverCandidates from JSON if no driver assigned yet
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<DriverCandidate> candidates = objectMapper.readValue(
                    match.getMatchedDriverCandidates(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, DriverCandidate.class)
                );
                response.setMatchedDriverCandidates(candidates);
                log.debug("✅ Loaded {} driver candidates from JSON for match {}", candidates.size(), matchId);
            } catch (Exception e) {
                log.warn("⚠️ Failed to parse matchedDriverCandidates for match {}: {}", matchId, e.getMessage());
            }
        }
        
        return response;
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
    public List<MatchResponse> getWaitingMatches() {
        List<Match> matches = matchRepository.findByStatus(Match.MatchStatus.WAITING);

        return matches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }
 
    @Override
    @Transactional
    public MatchResponse acceptRide(Long matchId, Long driverId) {
        // First try to find existing match
        Match match = matchRepository.findById(matchId).orElse(null);

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

        if (match != null) {
            // Existing match from database
            if (match.getStatus() != Match.MatchStatus.WAITING) {
                throw new IllegalArgumentException("Match is no longer available");
            }

            match.setDriver(driver);
            match.setVehicle(vehicle);
            match.setStatus(Match.MatchStatus.ACCEPTED);
            match = matchRepository.save(match);
        } else {
            // Check if this is a broadcast match (matchId is actually passengerId)
            BroadcastPassengerRequest broadcast = passengerBroadcastMap.get(matchId);
            if (broadcast == null) {
                throw new ResourceNotFoundException("Broadcast match not found for passenger: " + matchId);
            }

            // Create new match from broadcast data
            User passenger = userRepository.findById(matchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Passenger not found with id: " + matchId));

            if (vehicle == null) {
                throw new IllegalArgumentException("Driver does not have an approved vehicle");
            }

            match = new Match();
            match.setPassenger(passenger);
            match.setDriver(driver);
            match.setVehicle(vehicle);
            match.setPickupAddress(broadcast.getPickupAddress());
            match.setDestinationAddress(broadcast.getDestinationAddress());
            match.setPickupLatitude(broadcast.getPickupLatitude());
            match.setPickupLongitude(broadcast.getPickupLongitude());
            match.setDestinationLatitude(broadcast.getDestinationLatitude());
            match.setDestinationLongitude(broadcast.getDestinationLongitude());
            
            try {
                Integer calculatedCoin = coinCalculationService.calculateCoinFromCoordinates(
                    broadcast.getPickupLatitude(), broadcast.getPickupLongitude(),
                    broadcast.getDestinationLatitude(), broadcast.getDestinationLongitude()
                );
                match.setCoin(calculatedCoin != null ? calculatedCoin : 25);
                match.setFare(25); // Default fare, should be calculated based on pricing rules
            } catch (Exception e) {
                log.warn("Failed to calculate coin cost, using default: {}", e.getMessage());
                match.setCoin(25); // Default fallback
                match.setFare(25); // Default fare fallback
            }
            
            match.setStatus(Match.MatchStatus.ACCEPTED);
            match = matchRepository.save(match);

            // Remove from broadcast maps
            passengerBroadcastMap.remove(matchId);
            passengerBroadcasts.remove(broadcast);
        }

        // ===== UPDATE DRIVER METRICS =====
        driver.setTotalRidesAccepted(driver.getTotalRidesAccepted() + 1);
        driver.setDriverStatus(User.DriverStatus.BUSY); // Driver is now busy
        userRepository.save(driver);

        log.info("Driver {} accepted match {}. Total rides accepted: {}", 
                driverId, match.getId(), driver.getTotalRidesAccepted());

        // ✅ Build response with CURRENT driver location from User entity
        MatchResponse response = matchMapper.toResponse(match);
        
        // Create a single DriverCandidate with current driver location
        if (driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null) {
            DriverCandidate currentDriverInfo = DriverCandidate.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getFullName())
                    .driverPhone(driver.getPhoneNumber())
                    .vehicleId(vehicle.getId())
                    .vehicleInfo(vehicle.getMake() + " " + vehicle.getModel() + " - " + vehicle.getLicensePlate())
                    .currentLatitude(driver.getCurrentLatitude())
                    .currentLongitude(driver.getCurrentLongitude())
                    .driverRating(driver.getRating())
                    .build();
            
            response.setMatchedDriverCandidates(List.of(currentDriverInfo));
            
            log.info("✅ Driver {} current location: ({}, {})", 
                    driverId, driver.getCurrentLatitude(), driver.getCurrentLongitude());
        } else {
            log.warn("⚠️ Driver {} has no current location in database", driverId);
        }
        
        // ===== SEND NOTIFICATION TO PASSENGER =====
        try {
            String title = "Ride Accepted!";
            String body = "Driver " + driver.getFullName() + " has accepted your request and is on the way.";
            notificationService.sendNotification(match.getPassenger(), title, body, "MATCH_ACCEPTED", match.getId());
        } catch (Exception e) {
            log.error("Failed to send MATCH_ACCEPTED notification: {}", e.getMessage());
        }

        return response;
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

        // ===== SEND REAL-TIME NOTIFICATIONS =====
        try {
            switch (newStatus) {
                case WAITING: // Driver arrived (custom mapping if needed, or if updated from ARRIVED)
                     // If you have a specific ARRIVED status, use that. Assuming WAITING is not meaning arrived here.
                     // Based on context, let's look for valid statuses.
                     // If logic requires specific status for 'Arrived', ensure it exists. 
                     // For now, I will add notifications for COMPLETED and CANCELLED as they are standard.
                     break;
                     
                case IN_PROGRESS: // Trip Started
                    notificationService.sendNotification(
                        match.getPassenger(), 
                        "Trip Started", 
                        "Your ride to " + match.getDestinationAddress() + " has started.", 
                        "TRIP_STARTED", 
                        match.getId()
                    );
                    break;

                case COMPLETED:
                    notificationService.sendNotification(
                        match.getPassenger(), 
                        "Ride Completed", 
                        "You have arrived at your destination. total: " + match.getFare() + " coins.", 
                        "RIDE_COMPLETED", 
                        match.getId()
                    );
                    break;

                case CANCELLED:
                    User targetUser = isPassenger ? match.getDriver() : match.getPassenger();
                    if (targetUser != null) {
                        notificationService.sendNotification(
                            targetUser, 
                            "Ride Cancelled", 
                            "The ride has been cancelled by " + (isPassenger ? "passenger" : "driver") + ".", 
                            "MATCH_CANCELLED", 
                            match.getId()
                        );
                    }
                    break;
            }
            
            // Special case for 'ARRIVED' if it exists in Enum or is mapped from somewhere else.
            // If the request passes a status that isn't in the entity but is logical, we might need to handle it.
            // However, assuming `MatchStatus` matches the Entity definition.
            
        } catch (Exception e) {
            log.error("Failed to send status update notification: {}", e.getMessage());
        }

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

        match = matchRepository.save(match);

        // ===== SEND NOTIFICATION =====
        try {
            User targetUser = isPassenger ? match.getDriver() : match.getPassenger();
            if (targetUser != null) {
                notificationService.sendNotification(
                    targetUser, 
                    "Ride Cancelled", 
                    "The ride has been cancelled by " + (isPassenger ? "the passenger" : "the driver") + ".", 
                    "MATCH_CANCELLED", 
                    match.getId()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send MATCH_CANCELLED notification: {}", e.getMessage());
        }

        // Kết thúc session ngay lập tức
        try {
            sessionService.endSession(matchId);
        } catch (Exception e) {
            // Bỏ qua nếu session đã đóng
        }

        return matchMapper.toResponse(match);
    }

    @Override
    public void broadcastAsDriver(Long driverId, BroadcastDriverRequest request) {
        log.info("Driver {} broadcasting for passengers from ({}, {}) to ({}, {})",
                driverId, request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude());

        driverBroadcastMap.put(driverId, request);
        driverBroadcasts.add(request);
    }

    @Override
    public void broadcastAsPassenger(Long passengerId, BroadcastPassengerRequest request) {
        log.info("Passenger {} broadcasting for drivers from ({}, {}) to ({}, {})",
                passengerId, request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude());

        passengerBroadcastMap.put(passengerId, request);
        passengerBroadcasts.add(request);
    }

    @Override
    public List<MatchResponse> findMatches(Long userId, FindMatchesRequest request) {
        List<MatchResponse> matches = new java.util.ArrayList<>();

        if ("driver".equals(request.getType())) {
            // Driver tìm passengers đang broadcast
            for (Map.Entry<Long, BroadcastPassengerRequest> entry : passengerBroadcastMap.entrySet()) {
                BroadcastPassengerRequest broadcast = entry.getValue();
                // Simple matching: check if locations are close (within 5km)
                double distance = calculateDistance(
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    broadcast.getPickupLatitude(), broadcast.getPickupLongitude()
                );
                if (distance <= 5.0) { // 5km radius
                    // Create a MatchResponse for display
                    MatchResponse response = new MatchResponse();
                    response.setId(entry.getKey()); // Use passenger ID as match ID temporarily
                    response.setPassengerId(entry.getKey()); // Set real passenger ID
                    response.setPickupAddress(broadcast.getPickupAddress());
                    response.setDestinationAddress(broadcast.getDestinationAddress());
                    response.setPickupLatitude(broadcast.getPickupLatitude());
                    response.setPickupLongitude(broadcast.getPickupLongitude());
                    response.setDestinationLatitude(broadcast.getDestinationLatitude());
                    response.setDestinationLongitude(broadcast.getDestinationLongitude());
                    
                    // Fetch real passenger info
                    try {
                        UserDto passenger = userService.getUserById(entry.getKey());
                        response.setPassengerName(passenger.getFullName() != null ? passenger.getFullName() : "Passenger " + entry.getKey());
                        response.setPassengerPhone(passenger.getPhoneNumber());
                        response.setPassengerAvatar(passenger.getProfilePictureUrl() != null ? passenger.getProfilePictureUrl() : "https://i.pravatar.cc/150?img=" + entry.getKey());
                        response.setPassengerRating(4.5); // Default rating, could be calculated from reviews
                        response.setPassengerReviews(10); // Default reviews count
                    } catch (Exception e) {
                        // Fallback to mock data if user fetch fails
                        response.setPassengerName("Passenger " + entry.getKey());
                        response.setPassengerPhone("0901234567");
                        response.setPassengerAvatar("https://i.pravatar.cc/150?img=" + entry.getKey());
                        response.setPassengerRating(4.5);
                        response.setPassengerReviews(10);
                    }
                    
                    matches.add(response);
                }
            }
        } else if ("passenger".equals(request.getType())) {
            // Passenger tìm drivers đang broadcast
            for (Map.Entry<Long, BroadcastDriverRequest> entry : driverBroadcastMap.entrySet()) {
                BroadcastDriverRequest broadcast = entry.getValue();
                // Simple matching: check if locations are close (within 5km)
                double distance = calculateDistance(
                    request.getPickupLatitude(), request.getPickupLongitude(),
                    broadcast.getPickupLatitude(), broadcast.getPickupLongitude()
                );
                if (distance <= 5.0) { // 5km radius
                    // Create a MatchResponse for display
                    MatchResponse response = new MatchResponse();
                    response.setId(entry.getKey()); // Use driver ID as match ID temporarily
                    response.setDriverId(entry.getKey()); // Set real driver ID
                    response.setPickupAddress(broadcast.getPickupAddress());
                    response.setDestinationAddress(broadcast.getDestinationAddress());
                    response.setPickupLatitude(broadcast.getPickupLatitude());
                    response.setPickupLongitude(broadcast.getPickupLongitude());
                    response.setDestinationLatitude(broadcast.getDestinationLatitude());
                    response.setDestinationLongitude(broadcast.getDestinationLongitude());
                    response.setEstimatedPrice(broadcast.getEstimatedPrice());
                    
                    // Fetch real driver info
                    try {
                        UserDto driver = userService.getUserById(entry.getKey());
                        response.setDriverName(driver.getFullName() != null ? driver.getFullName() : "Driver " + entry.getKey());
                        response.setDriverPhone(driver.getPhoneNumber());
                        response.setDriverAvatar(driver.getProfilePictureUrl() != null ? driver.getProfilePictureUrl() : "https://i.pravatar.cc/150?img=" + (entry.getKey() + 10));
                        response.setDriverRating(4.7); // Default rating, could be calculated from reviews
                        
                        // Fetch driver's approved vehicle
                        List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndStatus(entry.getKey(), Vehicle.VehicleStatus.APPROVED);
                        if (!vehicles.isEmpty()) {
                            Vehicle vehicle = vehicles.get(0);
                            response.setVehicleModel(vehicle.getMake() + " " + vehicle.getModel());
                            response.setLicensePlate(vehicle.getLicensePlate());
                        } else {
                            response.setVehicleModel("Toyota Vios");
                            response.setLicensePlate("30A-12345");
                        }
                    } catch (Exception e) {
                        // Fallback to mock data if user fetch fails
                        response.setDriverName("Driver " + entry.getKey());
                        response.setDriverPhone("0901234568");
                        response.setDriverAvatar("https://i.pravatar.cc/150?img=" + (entry.getKey() + 10));
                        response.setDriverRating(4.7);
                        response.setVehicleModel("Toyota Vios");
                        response.setLicensePlate("30A-12345");
                    }
                    
                    matches.add(response);
                }
            }
        }

        log.info("Found {} matches for user {} of type {}", matches.size(), userId, request.getType());
        return matches;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        return distance;
    }
}
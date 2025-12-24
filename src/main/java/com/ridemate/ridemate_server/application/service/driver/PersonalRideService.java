package com.ridemate.ridemate_server.application.service.driver;

import com.ridemate.ridemate_server.application.dto.driver.StartPersonalRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.application.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalRideService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;

    /**
     * Start a personal ride for driver
     * Creates a match with DRIVER_ARRIVED status (skip phase 1: driver → pickup)
     * Only runs phase 2: pickup → destination
     */
    @Transactional
    public MatchResponse startPersonalRide(Long driverId, StartPersonalRideRequest request) {
        log.info("Starting personal ride for driver: {}", driverId);

        // Get driver
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        if (driver.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("Only drivers can start personal rides");
        }

        // Determine passenger: if ID provided in request, use that User; otherwise default to driver
        User passenger = driver;
        if (request.getPassengerId() != null) {
            passenger = userRepository.findById(request.getPassengerId())
                    .orElse(driver); // Fallback to driver if not found (or throw exception)
            log.info("Starting personal ride with specific passenger: {}", passenger.getId());
        }

        // Create match with DRIVER_ARRIVED status (skip phase 1)
        Match match = Match.builder()
                .driver(driver)
                .passenger(passenger) // Use determined passenger
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .pickupAddress(request.getPickupAddress())
                .destinationAddress(request.getDestinationAddress())
                .status(Match.MatchStatus.IN_PROGRESS) // Skip phase 1
                .matchedAt(LocalDateTime.now())
                .build();

        match = matchRepository.save(match);
        log.info("Created personal ride match: {} with status IN_PROGRESS", match.getId());

        // Create session immediately
        Session session = Session.builder()
                .match(match)
                .startTime(LocalDateTime.now())
                .isActive(true)
                .build();

        sessionRepository.save(session);
        log.info("Created session for personal ride: {}", session.getId());

        // Notify passenger if this is not a self-ride
        if (!passenger.getId().equals(driver.getId())) {
            try {
                notificationService.sendNotification(
                        passenger,
                        "Tài xế bắt đầu chuyến đi",
                        "Chuyến đi của bạn đã bắt đầu",
                        "TRIP_STARTED", // Consistent type for frontend mapping
                        match.getId()
                );
                log.info("Sent TRIP_STARTED notification to passenger {}", passenger.getId());
            } catch (Exception e) {
                log.error("Failed to send notification: {}", e.getMessage());
            }
        }

        // Build response
        return MatchResponse.builder()
                .id(match.getId())
                .driverId(driver.getId())
                .driverName(driver.getFullName())
                .driverPhone(driver.getPhoneNumber())
                .driverRating(driver.getRating() != null ? driver.getRating().doubleValue() : 0.0)
                .driverAvatar(driver.getProfilePictureUrl())
                .driverCurrentLatitude(driver.getCurrentLatitude())
                .driverCurrentLongitude(driver.getCurrentLongitude())
                .passengerId(passenger.getId())
                .passengerName(passenger.getFullName())
                .passengerPhone(passenger.getPhoneNumber())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .pickupAddress(request.getPickupAddress())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .destinationAddress(request.getDestinationAddress())
                .status(Match.MatchStatus.IN_PROGRESS.name())
                .createdAt(match.getMatchedAt())
                .sessionId(session.getId())
                .build();
    }
}

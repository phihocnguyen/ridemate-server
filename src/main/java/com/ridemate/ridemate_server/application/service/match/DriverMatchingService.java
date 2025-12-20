package com.ridemate.ridemate_server.application.service.match;

import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Vehicle;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Advanced Driver Matching Service
 * Implements Uber/Grab-like matching algorithm:
 * 1. Haversine distance calculation
 * 2. Multi-factor scoring (distance, rating, acceptance rate, ETA)
 * 3. Sequential batch matching with timeout
 */
@Service
@Slf4j
public class DriverMatchingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Configuration constants
    private static final double INITIAL_SEARCH_RADIUS_KM = 2.0;  // Start with 2km (priority range)
    private static final double MAX_SEARCH_RADIUS_KM = 7.0;      // Maximum 7km
    private static final double RADIUS_INCREMENT_KM = 2.0;       // Expand by 2km each time (2km -> 4km -> 6km -> 7km)
    private static final int MAX_CANDIDATES_TO_RETURN = 5;
    
    // Scoring weights (must sum to 1.0)
    private static final double WEIGHT_DISTANCE = 0.40;
    private static final double WEIGHT_RATING = 0.25;
    private static final double WEIGHT_ACCEPTANCE_RATE = 0.20;
    private static final double WEIGHT_ETA = 0.10;
    private static final double WEIGHT_COMPLETION_RATE = 0.05;
    
    // ETA calculation: assume average speed 30 km/h in city
    private static final double AVERAGE_SPEED_KMH = 30.0;
    
    // Location staleness check: reject locations older than 5 minutes
    private static final int MAX_LOCATION_STALENESS_MINUTES = 5;

    /**
     * Find best matching drivers for a ride request
     * @param match The ride match request
     * @return List of top driver candidates, sorted by match score (highest first)
     */
    public List<DriverCandidate> findBestDrivers(Match match) {
        Double pickupLat = match.getPickupLatitude();
        Double pickupLon = match.getPickupLongitude();
        
        if (pickupLat == null || pickupLon == null) {
            log.warn("Pickup location not provided for match {}", match.getId());
            return new ArrayList<>();
        }

        List<DriverCandidate> candidates = new ArrayList<>();
        double searchRadius = INITIAL_SEARCH_RADIUS_KM;

        // Expand search radius until we find enough candidates
        while (candidates.size() < MAX_CANDIDATES_TO_RETURN && searchRadius <= MAX_SEARCH_RADIUS_KM) {
            log.info("Searching for drivers within {} km radius", searchRadius);
            candidates = searchDriversInRadius(pickupLat, pickupLon, searchRadius);
            
            if (candidates.isEmpty()) {
                searchRadius += RADIUS_INCREMENT_KM;
            } else {
                break;
            }
        }

        if (candidates.isEmpty()) {
            log.warn("No available drivers found within {} km for match {}", MAX_SEARCH_RADIUS_KM, match.getId());
            return new ArrayList<>();
        }

        // Calculate scores and sort
        candidates.forEach(this::calculateMatchScore);
        
        return candidates.stream()
                .sorted(Comparator.comparing(DriverCandidate::getMatchScore).reversed())
                .limit(MAX_CANDIDATES_TO_RETURN)
                .collect(Collectors.toList());
    }

    /**
     * Search for available drivers within a radius
     */
    private List<DriverCandidate> searchDriversInRadius(Double centerLat, Double centerLon, double radiusKm) {
        // Find all ONLINE drivers
        List<User> onlineDrivers = userRepository.findByUserTypeAndDriverStatus(
                User.UserType.DRIVER, 
                User.DriverStatus.ONLINE
        );

        List<DriverCandidate> candidates = new ArrayList<>();

        for (User driver : onlineDrivers) {
            // Skip if location not available or stale
            if (!isLocationValid(driver)) {
                continue;
            }

            // Calculate distance
            double distance = calculateHaversineDistance(
                    centerLat, centerLon,
                    driver.getCurrentLatitude(), driver.getCurrentLongitude()
            );

            // Skip if outside radius
            if (distance > radiusKm) {
                continue;
            }

            // Check if driver has an approved vehicle
            List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndStatus(
                    driver.getId(), 
                    Vehicle.VehicleStatus.APPROVED
            );

            if (vehicles.isEmpty()) {
                log.debug("Driver {} has no approved vehicle", driver.getId());
                continue;
            }

            Vehicle vehicle = vehicles.get(0);
            
            // Calculate ETA
            int eta = calculateETA(distance);

            // Create candidate
            DriverCandidate candidate = DriverCandidate.builder()
                    .driverId(driver.getId())
                    .driverName(driver.getFullName())
                    .driverPhone(driver.getPhoneNumber())
                    .vehicleId(vehicle.getId())
                    .vehicleInfo(vehicle.getMake() + " " + vehicle.getModel() + " - " + vehicle.getLicensePlate())
                    .currentLatitude(driver.getCurrentLatitude())
                    .currentLongitude(driver.getCurrentLongitude())
                    .distanceToPickup(distance)
                    .driverRating(driver.getRating())
                    .acceptanceRate(driver.getAcceptanceRate())
                    .completionRate(driver.getCompletionRate())
                    .totalRidesCompleted(driver.getTotalRidesCompleted())
                    .estimatedArrivalTime(eta)
                    .build();

            candidates.add(candidate);
        }

        return candidates;
    }

    /**
     * Calculate match score using weighted multi-factor algorithm
     * Score = w1*(1/distance) + w2*rating + w3*acceptanceRate + w4*(1/ETA) + w5*completionRate
     */
    private void calculateMatchScore(DriverCandidate candidate) {
        // Normalize factors to 0-1 range
        
        // Distance score: closer is better (inverse relationship)
        // Normalize: 1km = 1.0, 10km = 0.1
        double distanceScore = Math.min(1.0, 10.0 / Math.max(candidate.getDistanceToPickup(), 0.5));
        
        // Rating score: 0-5 scale normalized to 0-1
        double ratingScore = candidate.getDriverRating() / 5.0;
        
        // Acceptance rate score: already 0-100, normalize to 0-1
        double acceptanceScore = candidate.getAcceptanceRate() / 100.0;
        
        // ETA score: inverse relationship (faster is better)
        // Normalize: 5min = 1.0, 30min = 0.167
        double etaScore = Math.min(1.0, 30.0 / Math.max(candidate.getEstimatedArrivalTime(), 1.0));
        
        // Completion rate score: already 0-100, normalize to 0-1
        double completionScore = candidate.getCompletionRate() / 100.0;

        // Calculate weighted sum
        double score = WEIGHT_DISTANCE * distanceScore +
                       WEIGHT_RATING * ratingScore +
                       WEIGHT_ACCEPTANCE_RATE * acceptanceScore +
                       WEIGHT_ETA * etaScore +
                       WEIGHT_COMPLETION_RATE * completionScore;

        candidate.setMatchScore(score);
        
        log.debug("Driver {} score: {:.3f} (dist={:.2f}km, rating={:.1f}, acceptance={:.1f}%, ETA={}min)",
                candidate.getDriverId(), score, candidate.getDistanceToPickup(), 
                candidate.getDriverRating(), candidate.getAcceptanceRate(),
                candidate.getEstimatedArrivalTime());
    }

    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @return distance in kilometers
     */
    private double calculateHaversineDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate estimated time of arrival in minutes
     */
    private int calculateETA(double distanceKm) {
        double hours = distanceKm / AVERAGE_SPEED_KMH;
        return (int) Math.ceil(hours * 60); // Convert to minutes and round up
    }

    /**
     * Check if driver's location is valid and not stale
     */
    private boolean isLocationValid(User driver) {
        if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
            return false;
        }

        if (driver.getLastLocationUpdate() == null) {
            return true; // Allow if never updated (for backward compatibility)
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleThreshold = now.minusMinutes(MAX_LOCATION_STALENESS_MINUTES);
        
        return driver.getLastLocationUpdate().isAfter(staleThreshold);
    }
}

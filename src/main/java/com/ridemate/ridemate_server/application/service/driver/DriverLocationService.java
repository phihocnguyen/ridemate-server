package com.ridemate.ridemate_server.application.service.driver;

import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service to simulate continuous driver location updates
 * In production, this would be replaced with real GPS tracking from mobile app
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final UserRepository userRepository;
    private final SupabaseRealtimeService supabaseRealtimeService;
    private static final Random RANDOM = new Random();
    
    // Simulate drivers moving around Ho Chi Minh city center
    private static final double BASE_LAT = 10.7769;
    private static final double BASE_LON = 106.7009;
    private static final double MOVEMENT_RADIUS = 0.05; // ~5km radius

    /**
     * Simulate location update for a driver
     * In real app, this would be called from mobile app via API
     */
    @Transactional
    public void simulateDriverLocationUpdate(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        if (driver.getUserType() != User.UserType.DRIVER) {
            return;
        }

        // Only update location if driver is ONLINE and NOT in a session
        if (driver.getDriverStatus() != User.DriverStatus.ONLINE) {
            log.debug("Driver {} status is {}, skipping location update", driverId, driver.getDriverStatus());
            return;
        }

        // Simulate slight movement (random walk)
        double newLat = driver.getCurrentLatitude() + (RANDOM.nextDouble() - 0.5) * 0.01;
        double newLon = driver.getCurrentLongitude() + (RANDOM.nextDouble() - 0.5) * 0.01;

        driver.setCurrentLatitude(newLat);
        driver.setCurrentLongitude(newLon);
        driver.setLastLocationUpdate(LocalDateTime.now());

        userRepository.save(driver);

        supabaseRealtimeService.updateDriverLocation(
                driverId,
                newLat,
                newLon,
                driver.getDriverStatus().name()
        );

        log.debug("Driver {} location updated to ({}, {})", driverId, newLat, newLon);
    }

    /**
     * Get all currently ONLINE drivers
     */
    public List<User> getOnlineDrivers() {
        return userRepository.findByUserTypeAndDriverStatus(
                User.UserType.DRIVER,
                User.DriverStatus.ONLINE
        );
    }

    /**
     * Update multiple drivers' locations (for testing)
     */
    @Transactional
    public void simulateBatchLocationUpdate() {
        List<User> onlineDrivers = getOnlineDrivers();
        
        for (User driver : onlineDrivers) {
            try {
                simulateDriverLocationUpdate(driver.getId());
            } catch (Exception e) {
                log.warn("Error updating driver {} location", driver.getId(), e);
            }
        }

        if (!onlineDrivers.isEmpty()) {
            log.info("Updated locations for {} online drivers", onlineDrivers.size());
        }
    }

    /**
     * Set driver to a specific location (for testing)
     */
    @Transactional
    public void setDriverLocation(Long driverId, Double latitude, Double longitude) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setLastLocationUpdate(LocalDateTime.now());

        userRepository.save(driver);

        if (driver.getDriverStatus() == User.DriverStatus.ONLINE) {
            supabaseRealtimeService.updateDriverLocation(
                    driverId,
                    latitude,
                    longitude,
                    driver.getDriverStatus().name()
            );
        }

        log.info("Driver {} location set to ({}, {})", driverId, latitude, longitude);
    }

    @Transactional
    public void updateDriverLocation(Long driverId, Double latitude, Double longitude) {
        setDriverLocation(driverId, latitude, longitude);
    }

    @Transactional
    public void setDriverStatus(Long driverId, User.DriverStatus status) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        driver.setDriverStatus(status);
        userRepository.save(driver);

        if (status == User.DriverStatus.ONLINE) {
            supabaseRealtimeService.updateDriverLocation(
                    driverId,
                    driver.getCurrentLatitude(),
                    driver.getCurrentLongitude(),
                    status.name()
            );
        } else {
            supabaseRealtimeService.removeDriverLocation(driverId);
        }

        log.info("Driver {} status changed to {}", driverId, status);
    }

    public List<Map<String, Object>> getOnlineDriversWithLocations() {
        List<User> onlineDrivers = getOnlineDrivers();
        
        return onlineDrivers.stream()
                .map(driver -> {
                    Map<String, Object> driverData = new HashMap<>();
                    driverData.put("driver_id", driver.getId());
                    driverData.put("driver_name", driver.getFullName());
                    driverData.put("latitude", driver.getCurrentLatitude());
                    driverData.put("longitude", driver.getCurrentLongitude());
                    driverData.put("driver_status", driver.getDriverStatus().name());
                    driverData.put("last_updated", driver.getLastLocationUpdate());
                    return driverData;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void setDriverOnlineStatus(Long driverId, String status) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));

        User.DriverStatus driverStatus = User.DriverStatus.valueOf(status.toUpperCase());
        driver.setDriverStatus(driverStatus);
        
        if (driverStatus == User.DriverStatus.ONLINE) {
            if (driver.getCurrentLatitude() == null || driver.getCurrentLongitude() == null) {
                driver.setCurrentLatitude(BASE_LAT);
                driver.setCurrentLongitude(BASE_LON);
            }
            driver.setLastLocationUpdate(LocalDateTime.now());
            
            userRepository.save(driver);
            
            supabaseRealtimeService.updateDriverLocation(
                    driverId,
                    driver.getCurrentLatitude(),
                    driver.getCurrentLongitude(),
                    driverStatus.name()
            );
        } else {
            userRepository.save(driver);
            supabaseRealtimeService.removeDriverLocation(driverId);
        }

        log.info("Driver {} status changed to {}", driverId, driverStatus);
    }
}

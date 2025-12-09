package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.service.match.CoinCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of coin calculation service
 * 
 * Pricing Formula:
 * - Base coin: 10 coins (minimum charge)
 * - Per km: 5 coins/km
 * - Formula: coin = BASE_COIN + (distance * COIN_PER_KM)
 * 
 * Examples:
 * - 0.5 km → 10 + (0.5 * 5) = 12.5 → 13 coins
 * - 3 km → 10 + (3 * 5) = 25 coins
 * - 10 km → 10 + (10 * 5) = 60 coins
 */
@Slf4j
@Service
public class CoinCalculationServiceImpl implements CoinCalculationService {
    
    private static final int BASE_COIN = 10;           // Minimum charge
    private static final int COIN_PER_KM = 5;          // Price per km
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    @Override
    public Integer calculateCoin(Double distanceInKm) {
        if (distanceInKm == null || distanceInKm <= 0) {
            log.warn("Invalid distance: {}, returning base coin", distanceInKm);
            return BASE_COIN;
        }
        
        // Formula: BASE_COIN + (distance * COIN_PER_KM)
        double rawCoin = BASE_COIN + (distanceInKm * COIN_PER_KM);
        int coin = (int) Math.ceil(rawCoin); // Round up
        
        log.debug("Distance: {:.2f}km → Coin: {} (formula: {} + {:.2f} * {})", 
                distanceInKm, coin, BASE_COIN, distanceInKm, COIN_PER_KM);
        
        return coin;
    }
    
    @Override
    public Integer calculateCoinFromCoordinates(Double pickupLat, Double pickupLon, 
                                                 Double destLat, Double destLon) {
        if (pickupLat == null || pickupLon == null || destLat == null || destLon == null) {
            log.warn("Missing coordinates, returning base coin");
            return BASE_COIN;
        }
        
        double distance = calculateHaversineDistance(pickupLat, pickupLon, destLat, destLon);
        return calculateCoin(distance);
    }
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * Same formula used in DriverMatchingService for consistency
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c; // Distance in km
    }
}

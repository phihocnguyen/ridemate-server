package com.ridemate.ridemate_server.application.service.match;

/**
 * Service for calculating coin cost based on ride distance
 */
public interface CoinCalculationService {
    
    /**
     * Calculate coin cost based on distance
     * @param distanceInKm Distance in kilometers
     * @return Coin cost
     */
    Integer calculateCoin(Double distanceInKm);
    
    /**
     * Calculate coin cost from coordinates
     * @param pickupLat Pickup latitude
     * @param pickupLon Pickup longitude
     * @param destLat Destination latitude
     * @param destLon Destination longitude
     * @return Coin cost
     */
    Integer calculateCoinFromCoordinates(Double pickupLat, Double pickupLon, 
                                         Double destLat, Double destLon);
}

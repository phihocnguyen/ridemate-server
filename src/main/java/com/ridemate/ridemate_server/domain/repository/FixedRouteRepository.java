package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.FixedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface FixedRouteRepository extends JpaRepository<FixedRoute, Long> {

    /**
     * Find all active routes by driver
     */
    List<FixedRoute> findByDriverIdAndStatus(Long driverId, FixedRoute.RouteStatus status);

    /**
     * Find all active routes
     */
    List<FixedRoute> findByStatus(FixedRoute.RouteStatus status);

    /**
     * Find routes within proximity of pickup and dropoff locations
     * Uses Haversine formula to calculate distance
     */
    @Query(value = """
        SELECT r.* FROM fixed_routes r
        WHERE r.status = 'ACTIVE'
        AND r.available_seats > 0
        AND (
            6371000 * acos(
                cos(radians(:pickupLat)) * cos(radians(r.pickup_latitude)) *
                cos(radians(r.pickup_longitude) - radians(:pickupLon)) +
                sin(radians(:pickupLat)) * sin(radians(r.pickup_latitude))
            )
        ) <= r.pickup_radius
        AND (
            6371000 * acos(
                cos(radians(:dropoffLat)) * cos(radians(r.dropoff_latitude)) *
                cos(radians(r.dropoff_longitude) - radians(:dropoffLon)) +
                sin(radians(:dropoffLat)) * sin(radians(r.dropoff_latitude))
            )
        ) <= r.dropoff_radius
        ORDER BY r.departure_time ASC
        """, nativeQuery = true)
    List<FixedRoute> findRoutesNearLocations(
        @Param("pickupLat") Double pickupLat,
        @Param("pickupLon") Double pickupLon,
        @Param("dropoffLat") Double dropoffLat,
        @Param("dropoffLon") Double dropoffLon
    );

    /**
     * Find routes by specific date
     */
    @Query("SELECT r FROM FixedRoute r WHERE r.status = :status AND r.specificDates LIKE %:date%")
    List<FixedRoute> findByStatusAndSpecificDatesContaining(
        @Param("status") FixedRoute.RouteStatus status,
        @Param("date") String date
    );

    /**
     * Find routes by driver and status
     */
    @Query("SELECT r FROM FixedRoute r WHERE r.driver.id = :driverId AND r.status = :status ORDER BY r.departureTime ASC")
    List<FixedRoute> findByDriverIdAndStatusOrderByDepartureTime(
        @Param("driverId") Long driverId,
        @Param("status") FixedRoute.RouteStatus status
    );

    /**
     * Find routes departing after a specific time
     */
    @Query("SELECT r FROM FixedRoute r WHERE r.status = 'ACTIVE' AND r.departureTime >= :time ORDER BY r.departureTime ASC")
    List<FixedRoute> findActiveRoutesDepartingAfter(@Param("time") LocalTime time);

    /**
     * Count active routes by driver
     */
    @Query("SELECT COUNT(r) FROM FixedRoute r WHERE r.driver.id = :driverId AND r.status = 'ACTIVE'")
    Long countActiveRoutesByDriver(@Param("driverId") Long driverId);
}


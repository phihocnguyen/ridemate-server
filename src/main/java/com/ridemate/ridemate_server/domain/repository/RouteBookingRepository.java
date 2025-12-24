package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.RouteBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteBookingRepository extends JpaRepository<RouteBooking, Long> {

    /**
     * Find bookings by route ID
     */
    List<RouteBooking> findByRouteId(Long routeId);

    /**
     * Find bookings by passenger ID
     */
    List<RouteBooking> findByPassengerId(Long passengerId);

    /**
     * Find bookings by route and status
     */
    List<RouteBooking> findByRouteIdAndStatus(Long routeId, RouteBooking.BookingStatus status);

    /**
     * Find bookings by passenger and status
     */
    List<RouteBooking> findByPassengerIdAndStatus(Long passengerId, RouteBooking.BookingStatus status);

    /**
     * Find pending bookings for a specific route
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.route.id = :routeId AND b.status = 'PENDING' ORDER BY b.createdAt ASC")
    List<RouteBooking> findPendingBookingsByRoute(@Param("routeId") Long routeId);

    /**
     * Find bookings by route and booking date
     */
    List<RouteBooking> findByRouteIdAndBookingDate(Long routeId, LocalDate bookingDate);

    /**
     * Find accepted bookings for a route on a specific date
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.route.id = :routeId AND b.bookingDate = :date AND b.status IN ('ACCEPTED', 'IN_PROGRESS', 'COMPLETED')")
    List<RouteBooking> findAcceptedBookingsByRouteAndDate(
        @Param("routeId") Long routeId,
        @Param("date") LocalDate date
    );

    /**
     * Check if passenger has already booked this route for a specific date
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.route.id = :routeId AND b.passenger.id = :passengerId AND b.bookingDate = :date AND b.status NOT IN ('REJECTED', 'CANCELLED')")
    Optional<RouteBooking> findExistingBooking(
        @Param("routeId") Long routeId,
        @Param("passengerId") Long passengerId,
        @Param("date") LocalDate date
    );

    /**
     * Find bookings by driver (through route)
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.route.driver.id = :driverId ORDER BY b.createdAt DESC")
    List<RouteBooking> findByDriverId(@Param("driverId") Long driverId);

    /**
     * Find pending bookings for driver
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.route.driver.id = :driverId AND b.status = 'PENDING' ORDER BY b.createdAt ASC")
    List<RouteBooking> findPendingBookingsByDriver(@Param("driverId") Long driverId);

    /**
     * Count bookings by route and status
     */
    @Query("SELECT COUNT(b) FROM RouteBooking b WHERE b.route.id = :routeId AND b.status = :status")
    Long countByRouteIdAndStatus(@Param("routeId") Long routeId, @Param("status") RouteBooking.BookingStatus status);

    List<RouteBooking> findByMatchId(Long matchId);

    /**
     * Find bookings for a specific date range
     */
    @Query("SELECT b FROM RouteBooking b WHERE b.bookingDate BETWEEN :startDate AND :endDate ORDER BY b.bookingDate ASC")
    List<RouteBooking> findBookingsBetweenDates(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}


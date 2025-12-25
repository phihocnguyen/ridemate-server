package com.ridemate.ridemate_server.application.service.route.impl;

import com.ridemate.ridemate_server.application.dto.route.CreateRouteBookingRequest;
import com.ridemate.ridemate_server.application.dto.route.RouteBookingResponse;
import com.ridemate.ridemate_server.application.mapper.RouteBookingMapper;
import com.ridemate.ridemate_server.application.service.driver.SupabaseRealtimeService;
import com.ridemate.ridemate_server.application.service.notification.NotificationService;
import com.ridemate.ridemate_server.application.service.route.RouteBookingService;
import com.ridemate.ridemate_server.application.service.session.SessionService;
import com.ridemate.ridemate_server.domain.entity.*;
import com.ridemate.ridemate_server.domain.repository.*;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RouteBookingServiceImpl implements RouteBookingService {

    @Autowired
    private RouteBookingRepository routeBookingRepository;

    @Autowired
    private FixedRouteRepository fixedRouteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private RouteBookingMapper routeBookingMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SessionService sessionService;

    @Autowired(required = false)
    private SupabaseRealtimeService supabaseRealtimeService;

    @Override
    @Transactional
    public RouteBookingResponse createBooking(Long passengerId, CreateRouteBookingRequest request) {
        log.info("Creating booking for passenger {} on route {}", passengerId, request.getRouteId());

        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));

        FixedRoute route = fixedRouteRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // Validate route is active
        if (route.getStatus() != FixedRoute.RouteStatus.ACTIVE) {
            throw new IllegalArgumentException("Route is not active");
        }

        // Validate booking date
        LocalDate bookingDate = request.getBookingDate();
        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book for past dates");
        }

        // Validate booking date is in route's specific dates
        String dateStr = bookingDate.toString();
        if (!route.isAvailableOnDate(dateStr)) {
            throw new IllegalArgumentException("Route is not available on date: " + dateStr);
        }

        // Check if passenger already has a booking for this route on this date
        Optional<RouteBooking> existingBooking = routeBookingRepository.findExistingBooking(
                request.getRouteId(), passengerId, bookingDate);
        if (existingBooking.isPresent()) {
            throw new IllegalArgumentException("You already have a booking for this route on this date");
        }

        // Validate available seats
        if (route.getAvailableSeats() < request.getNumberOfSeats()) {
            throw new IllegalArgumentException("Not enough available seats");
        }

        // Calculate distances from passenger's location to route's pickup/dropoff
        Double pickupDistance = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                route.getPickupLatitude(), route.getPickupLongitude()
        );
        Double dropoffDistance = calculateDistance(
                request.getDropoffLatitude(), request.getDropoffLongitude(),
                route.getDropoffLatitude(), route.getDropoffLongitude()
        );

        // Validate proximity
        if (pickupDistance > route.getPickupRadius()) {
            throw new IllegalArgumentException("Pickup location is too far from route's pickup point");
        }
        if (dropoffDistance > route.getDropoffRadius()) {
            throw new IllegalArgumentException("Dropoff location is too far from route's dropoff point");
        }

        // Calculate total price
        Integer totalPrice = route.getPricePerSeat() * request.getNumberOfSeats();

        // Create booking
        RouteBooking booking = RouteBooking.builder()
                .route(route)
                .passenger(passenger)
                .pickupAddress(request.getPickupAddress())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .dropoffAddress(request.getDropoffAddress())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .bookingDate(bookingDate)
                .numberOfSeats(request.getNumberOfSeats())
                .totalPrice(totalPrice)
                .pickupDistanceFromRoute(pickupDistance)
                .dropoffDistanceFromRoute(dropoffDistance)
                .status(RouteBooking.BookingStatus.PENDING)
                .build();

        booking = routeBookingRepository.save(booking);
        log.info("Booking {} created successfully", booking.getId());

        // Send notification to driver
        notificationService.sendNotification(
                route.getDriver(),
                "Yêu cầu tham gia chuyến đi",
                String.format("%s muốn tham gia chuyến đi %s vào ngày %s",
                        passenger.getFullName(), route.getRouteName(), bookingDate),
                "MATCH_REQUEST",
                booking.getId()
        );

        log.info("Notification sent to driver {}", route.getDriver().getId());

        return routeBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public RouteBookingResponse acceptBooking(Long bookingId, Long driverId) {
        log.info("Driver {} accepting booking {}", driverId, bookingId);

        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate driver ownership
        if (!booking.getRoute().getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to accept this booking");
        }

        // Validate booking status
        if (booking.getStatus() != RouteBooking.BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not in pending status");
        }

        // Accept booking
        booking.accept();

        // Decrease available seats
        FixedRoute route = booking.getRoute();
        for (int i = 0; i < booking.getNumberOfSeats(); i++) {
            route.decreaseAvailableSeats();
        }
        fixedRouteRepository.save(route);

        booking = routeBookingRepository.save(booking);
        log.info("Booking {} accepted successfully", bookingId);

        // Send notification to passenger
        notificationService.sendNotification(
                booking.getPassenger(),
                "Yêu cầu được chấp nhận",
                String.format("Tài xế %s đã chấp nhận yêu cầu tham gia chuyến đi %s",
                        route.getDriver().getFullName(), route.getRouteName()),
                "MATCH_ACCEPTED",
                bookingId
        );

        log.info("Notification sent to passenger {}", booking.getPassenger().getId());

        return routeBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public RouteBookingResponse rejectBooking(Long bookingId, Long driverId) {
        log.info("Driver {} rejecting booking {}", driverId, bookingId);

        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate driver ownership
        if (!booking.getRoute().getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to reject this booking");
        }

        // Validate booking status
        if (booking.getStatus() != RouteBooking.BookingStatus.PENDING) {
            throw new IllegalArgumentException("Booking is not in pending status");
        }

        // Reject booking
        booking.reject();
        booking = routeBookingRepository.save(booking);
        log.info("Booking {} rejected successfully", bookingId);

        // Send notification to passenger
        notificationService.sendNotification(
                booking.getPassenger(),
                "Yêu cầu bị từ chối",
                String.format("Tài xế %s đã từ chối yêu cầu tham gia chuyến đi %s",
                        booking.getRoute().getDriver().getFullName(), booking.getRoute().getRouteName()),
                "MATCH_CANCELLED",
                bookingId
        );

        return routeBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public RouteBookingResponse cancelBooking(Long bookingId, Long passengerId) {
        log.info("Passenger {} cancelling booking {}", passengerId, bookingId);

        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate passenger ownership
        if (!booking.getPassenger().getId().equals(passengerId)) {
            throw new IllegalArgumentException("You don't have permission to cancel this booking");
        }

        // Can only cancel if pending or accepted
        if (booking.getStatus() != RouteBooking.BookingStatus.PENDING &&
                booking.getStatus() != RouteBooking.BookingStatus.ACCEPTED) {
            throw new IllegalArgumentException("Cannot cancel booking in current status");
        }

        // If booking was accepted, restore available seats
        if (booking.getStatus() == RouteBooking.BookingStatus.ACCEPTED) {
            FixedRoute route = booking.getRoute();
            for (int i = 0; i < booking.getNumberOfSeats(); i++) {
                route.increaseAvailableSeats();
            }
            fixedRouteRepository.save(route);
        }

        // Cancel booking
        booking.cancel();
        booking = routeBookingRepository.save(booking);
        log.info("Booking {} cancelled successfully", bookingId);

        // Send notification to driver
        notificationService.sendNotification(
                booking.getRoute().getDriver(),
                "Hành khách hủy chuyến",
                String.format("%s đã hủy yêu cầu tham gia chuyến đi %s",
                        booking.getPassenger().getFullName(), booking.getRoute().getRouteName()),
                "MATCH_CANCELLED",
                bookingId
        );

        return routeBookingMapper.toResponse(booking);
    }

    @Override
    public RouteBookingResponse getBookingById(Long bookingId) {
        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return routeBookingMapper.toResponse(booking);
    }

    @Override
    public List<RouteBookingResponse> getBookingsByPassenger(Long passengerId) {
        List<RouteBooking> bookings = routeBookingRepository.findByPassengerId(passengerId);
        return bookings.stream()
                .map(routeBookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteBookingResponse> getBookingsByDriver(Long driverId) {
        List<RouteBooking> bookings = routeBookingRepository.findByDriverId(driverId);
        return bookings.stream()
                .map(routeBookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteBookingResponse> getPendingBookingsByDriver(Long driverId) {
        List<RouteBooking> bookings = routeBookingRepository.findPendingBookingsByDriver(driverId);
        return bookings.stream()
                .map(routeBookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RouteBookingResponse> getBookingsByRoute(Long routeId) {
        List<RouteBooking> bookings = routeBookingRepository.findByRouteId(routeId);
        return bookings.stream()
                .map(routeBookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RouteBookingResponse startTrip(Long bookingId, Long driverId) {
        log.error("🚀🚀🚀 RouteBookingServiceImpl.startTrip CALLED - Driver {} starting trip for booking {} 🚀🚀🚀", driverId, bookingId);
        System.out.println("🚀🚀🚀 RouteBookingServiceImpl.startTrip CALLED - Driver " + driverId + " starting trip for booking " + bookingId + " 🚀🚀🚀");

        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate driver ownership
        if (!booking.getRoute().getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to start this trip");
        }

        // Validate booking status - Allow both ACCEPTED and IN_PROGRESS
        // IN_PROGRESS is allowed to ensure match is published to Supabase even if already started
        if (booking.getStatus() != RouteBooking.BookingStatus.ACCEPTED && 
            booking.getStatus() != RouteBooking.BookingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Booking must be accepted or in progress before starting trip");
        }
        
        // If already IN_PROGRESS, check if match exists and just publish it
        if (booking.getStatus() == RouteBooking.BookingStatus.IN_PROGRESS && booking.getMatch() != null) {
            log.info("Booking {} is already IN_PROGRESS with match {}, will publish match to Supabase", 
                    bookingId, booking.getMatch().getId());
            
            Match match = booking.getMatch();
            
            // Reload match from DB to ensure we have all fields
            match = matchRepository.findById(match.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
            
            // Publish match to Supabase to trigger realtime updates for passenger
            log.info("🚀 Attempting to publish existing match {} to Supabase (status: {})", match.getId(), match.getStatus());
            
            if (supabaseRealtimeService == null) {
                log.error("❌ SupabaseRealtimeService is NULL! Match {} will NOT be published to Supabase. Check Supabase configuration.", match.getId());
            } else {
                log.info("✅ SupabaseRealtimeService is available, proceeding with publish...");
                
                try {
                    // Send minimal fields + passenger_id + status for realtime filtering
                    Map<String, Object> matchData = new HashMap<>();
                    matchData.put("id", match.getId());
                    matchData.put("passenger_id", match.getPassenger().getId());
                    matchData.put("status", match.getStatus().name()); // ✅ Added for frontend condition check
                    if (match.getDriver() != null) {
                        matchData.put("driver_id", match.getDriver().getId());
                    } else {
                        log.warn("⚠️ Match {} has no driver!", match.getId());
                    }
                    matchData.put("pickup_latitude", match.getPickupLatitude());
                    matchData.put("pickup_longitude", match.getPickupLongitude());
                    matchData.put("destination_latitude", match.getDestinationLatitude());
                    matchData.put("destination_longitude", match.getDestinationLongitude());
                    
                    log.info("📤 Publishing existing match {} to Supabase with data: {}", match.getId(), matchData);
                    supabaseRealtimeService.publishMatch(matchData);
                    log.info("✅ Published existing match {} to Supabase for realtime passenger notification", match.getId());
                } catch (Exception e) {
                    log.error("❌ CRITICAL: Failed to publish existing match {} to Supabase: {}", match.getId(), e.getMessage(), e);
                    e.printStackTrace();
                }
            }
            
            return routeBookingMapper.toResponse(booking);
        }

        // Create Match entity (similar to existing ride flow)
        FixedRoute route = booking.getRoute();
        Match match = Match.builder()
                .passenger(booking.getPassenger())
                .driver(route.getDriver())
                .vehicle(route.getVehicle())
                .pickupAddress(booking.getPickupAddress())
                .destinationAddress(booking.getDropoffAddress())
                .pickupLatitude(booking.getPickupLatitude())
                .pickupLongitude(booking.getPickupLongitude())
                .destinationLatitude(booking.getDropoffLatitude())
                .destinationLongitude(booking.getDropoffLongitude())
                .distance(route.getDistance())
                .coin(booking.getTotalPrice())
                .fare(booking.getTotalPrice())
                .status(Match.MatchStatus.IN_PROGRESS)
                .build();

        match = matchRepository.save(match);
        log.info("Match {} created for booking {}", match.getId(), bookingId);

        // Create Session
        sessionService.createSession(match);
        log.info("Session created for match {}", match.getId());

        // Update booking
        booking.setMatch(match);
        booking.startTrip();
        booking = routeBookingRepository.save(booking);

        log.info("Trip started for booking {}", bookingId);

        // Reload match from DB to ensure we have all fields including createdAt
        match = matchRepository.findById(match.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found after creation"));

        // Publish match to Supabase to trigger realtime updates for passenger
        // This will allow passenger's listener in Home.js to receive the match and navigate to maps
        log.info("🚀 Attempting to publish match {} to Supabase (status: {})", match.getId(), match.getStatus());
        
        if (supabaseRealtimeService == null) {
            log.error("❌ SupabaseRealtimeService is NULL! Match {} will NOT be published to Supabase. Check Supabase configuration.", match.getId());
        } else {
            log.info("✅ SupabaseRealtimeService is available, proceeding with publish...");
            
            try {
                // Send minimal fields + passenger_id + status for realtime filtering
                Map<String, Object> matchData = new HashMap<>();
                matchData.put("id", match.getId());
                matchData.put("passenger_id", match.getPassenger().getId());
                matchData.put("status", match.getStatus().name()); // ✅ Added for frontend condition check
                if (match.getDriver() != null) {
                    matchData.put("driver_id", match.getDriver().getId());
                } else {
                    log.warn("⚠️ Match {} has no driver!", match.getId());
                }
                matchData.put("pickup_latitude", match.getPickupLatitude());
                matchData.put("pickup_longitude", match.getPickupLongitude());
                matchData.put("destination_latitude", match.getDestinationLatitude());
                matchData.put("destination_longitude", match.getDestinationLongitude());
                
                log.info("📤 Calling publishMatch() for match {} to Supabase (async operation)...", match.getId());
                log.debug("Match data to publish: {}", matchData);
                supabaseRealtimeService.publishMatch(matchData);
                log.info("✅ publishMatch() called for match {} (check SupabaseRealtimeService logs for actual publish result)", match.getId());
            } catch (Exception e) {
                log.error("❌ CRITICAL: Failed to publish match {} to Supabase: {}", match.getId(), e.getMessage(), e);
                e.printStackTrace();
            }
        }

        // Send notification to passenger that driver has started the trip
        // This will trigger passenger to display maps via Supabase Realtime
        try {
            notificationService.sendNotification(
                    booking.getPassenger(),
                    "Tài xế bắt đầu chuyến đi",
                    String.format("Tài xế đã bắt đầu chuyến đi %s. Vui lòng theo dõi trên bản đồ.", route.getRouteName()),
                    "TRIP_STARTED",
                    match.getId() // Use match ID so passenger can navigate to MatchedRideScreen
            );
            log.info("Sent TRIP_STARTED notification to passenger {} for match {}", 
                    booking.getPassenger().getId(), match.getId());
        } catch (Exception e) {
            log.error("Failed to send notification to passenger: {}", e.getMessage());
        }

        return routeBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public RouteBookingResponse completeTrip(Long bookingId, Long driverId) {
        log.info("Driver {} completing trip for booking {}", driverId, bookingId);

        RouteBooking booking = routeBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Validate driver ownership
        if (!booking.getRoute().getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to complete this trip");
        }

        // Validate booking status
        if (booking.getStatus() != RouteBooking.BookingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Trip is not in progress");
        }

        // Update match status
        if (booking.getMatch() != null) {
            Match match = booking.getMatch();
            match.setStatus(Match.MatchStatus.COMPLETED);
            matchRepository.save(match);
        }

        // Complete booking
        booking.complete();
        booking = routeBookingRepository.save(booking);

        log.info("Trip completed for booking {}", bookingId);

        // Send notification to passenger
        notificationService.sendNotification(
                booking.getPassenger(),
                "Chuyến đi hoàn thành",
                String.format("Chuyến đi %s đã hoàn thành", booking.getRoute().getRouteName()),
                "RIDE_COMPLETED",
                bookingId
        );

        // Send notification to driver
        notificationService.sendNotification(
                booking.getRoute().getDriver(),
                "Chuyến đi hoàn thành",
                String.format("Bạn đã hoàn thành chuyến đi %s", booking.getRoute().getRouteName()),
                "RIDE_COMPLETED",
                bookingId
        );

        return routeBookingMapper.toResponse(booking);
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in meters
     */
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Convert DayOfWeek to day code
     */
    private String getDayCode(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }
}


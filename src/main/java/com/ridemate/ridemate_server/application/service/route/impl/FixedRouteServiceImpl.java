package com.ridemate.ridemate_server.application.service.route.impl;

import com.ridemate.ridemate_server.application.dto.route.*;
import com.ridemate.ridemate_server.application.mapper.FixedRouteMapper;
import com.ridemate.ridemate_server.application.service.route.FixedRouteService;
import com.ridemate.ridemate_server.domain.entity.FixedRoute;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Vehicle;
import com.ridemate.ridemate_server.domain.repository.FixedRouteRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FixedRouteServiceImpl implements FixedRouteService {

    @Autowired
    private FixedRouteRepository fixedRouteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private FixedRouteMapper fixedRouteMapper;

    @Override
    @Transactional
    public FixedRouteResponse createRoute(Long driverId, CreateFixedRouteRequest request) {
        log.info("Creating fixed route for driver {}", driverId);

        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        // Validate driver
        if (driver.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("User is not a driver");
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        // Validate vehicle belongs to driver
        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("Vehicle does not belong to driver");
        }

        // Validate vehicle is approved
        if (vehicle.getStatus() != Vehicle.VehicleStatus.APPROVED) {
            throw new IllegalArgumentException("Vehicle is not approved");
        }

        // Calculate distance between pickup and dropoff
        Double distance = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );

        FixedRoute route = FixedRoute.builder()
                .driver(driver)
                .vehicle(vehicle)
                .routeName(request.getRouteName())
                .description(request.getDescription())
                .pickupAddress(request.getPickupAddress())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .dropoffAddress(request.getDropoffAddress())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .departureTime(request.getDepartureTime())
                .specificDates(request.getSpecificDates())
                .pricePerSeat(request.getPricePerSeat())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats()) // Initially all seats available
                .distance(distance)
                .pickupRadius(request.getPickupRadius() != null ? request.getPickupRadius() : 500)
                .dropoffRadius(request.getDropoffRadius() != null ? request.getDropoffRadius() : 500)
                .status(FixedRoute.RouteStatus.ACTIVE)
                .build();

        route = fixedRouteRepository.save(route);
        log.info("Fixed route {} created successfully", route.getId());

        return fixedRouteMapper.toResponse(route);
    }

    @Override
    @Transactional
    public FixedRouteResponse updateRoute(Long routeId, Long driverId, UpdateFixedRouteRequest request) {
        log.info("Updating fixed route {} by driver {}", routeId, driverId);

        FixedRoute route = fixedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // Validate ownership
        if (!route.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to update this route");
        }

        // Update fields if provided
        if (request.getRouteName() != null) {
            route.setRouteName(request.getRouteName());
        }
        if (request.getDescription() != null) {
            route.setDescription(request.getDescription());
        }
        if (request.getDepartureTime() != null) {
            route.setDepartureTime(request.getDepartureTime());
        }
        if (request.getSpecificDates() != null) {
            route.setSpecificDates(request.getSpecificDates());
        }
        if (request.getPricePerSeat() != null) {
            route.setPricePerSeat(request.getPricePerSeat());
        }
        if (request.getTotalSeats() != null) {
            // Adjust available seats proportionally
            int seatsDiff = request.getTotalSeats() - route.getTotalSeats();
            route.setTotalSeats(request.getTotalSeats());
            route.setAvailableSeats(Math.max(0, route.getAvailableSeats() + seatsDiff));
        }
        if (request.getPickupRadius() != null) {
            route.setPickupRadius(request.getPickupRadius());
        }
        if (request.getDropoffRadius() != null) {
            route.setDropoffRadius(request.getDropoffRadius());
        }
        if (request.getStatus() != null) {
            route.setStatus(FixedRoute.RouteStatus.valueOf(request.getStatus()));
        }

        route = fixedRouteRepository.save(route);
        log.info("Fixed route {} updated successfully", routeId);

        return fixedRouteMapper.toResponse(route);
    }

    @Override
    @Transactional
    public void deleteRoute(Long routeId, Long driverId) {
        log.info("Deleting fixed route {} by driver {}", routeId, driverId);

        FixedRoute route = fixedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // Validate ownership
        if (!route.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to delete this route");
        }

        // Soft delete by setting status to CANCELLED
        route.setStatus(FixedRoute.RouteStatus.CANCELLED);
        fixedRouteRepository.save(route);

        log.info("Fixed route {} deleted successfully", routeId);
    }

    @Override
    public FixedRouteResponse getRouteById(Long routeId) {
        FixedRoute route = fixedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        return fixedRouteMapper.toResponse(route);
    }

    @Override
    public List<FixedRouteResponse> getRoutesByDriver(Long driverId) {
        List<FixedRoute> routes = fixedRouteRepository.findByDriverIdAndStatusOrderByDepartureTime(
                driverId, FixedRoute.RouteStatus.ACTIVE);
        return routes.stream()
                .map(fixedRouteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FixedRouteResponse> getAllActiveRoutes() {
        List<FixedRoute> routes = fixedRouteRepository.findByStatus(FixedRoute.RouteStatus.ACTIVE);
        return routes.stream()
                .map(fixedRouteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FixedRouteResponse> searchRoutes(SearchFixedRoutesRequest request) {
        log.info("Searching routes near pickup ({}, {}) and dropoff ({}, {})",
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude());

        // Find routes within proximity
        List<FixedRoute> routes = fixedRouteRepository.findRoutesNearLocations(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );

        // Filter by travel date if provided
        if (request.getTravelDate() != null) {
            LocalDate travelDate = request.getTravelDate();
            DayOfWeek dayOfWeek = travelDate.getDayOfWeek();
            // Filter by specific date if provided
            String dateStr = request.getTravelDate() != null ? request.getTravelDate().toString() : null;
            if (dateStr != null) {
                routes = routes.stream()
                        .filter(route -> route.isAvailableOnDate(dateStr))
                        .collect(Collectors.toList());
            }
        }

        // Filter by number of seats if provided
        if (request.getNumberOfSeats() != null) {
            routes = routes.stream()
                    .filter(route -> route.getAvailableSeats() >= request.getNumberOfSeats())
                    .collect(Collectors.toList());
        }

        // Convert to response and calculate distances
        List<FixedRouteResponse> responses = routes.stream()
                .map(route -> {
                    FixedRouteResponse response = fixedRouteMapper.toResponse(route);
                    
                    // Calculate distance from user's location to route's pickup/dropoff
                    Double pickupDist = calculateDistance(
                            request.getPickupLatitude(), request.getPickupLongitude(),
                            route.getPickupLatitude(), route.getPickupLongitude()
                    );
                    Double dropoffDist = calculateDistance(
                            request.getDropoffLatitude(), request.getDropoffLongitude(),
                            route.getDropoffLatitude(), route.getDropoffLongitude()
                    );
                    
                    response.setPickupDistanceFromUser(pickupDist);
                    response.setDropoffDistanceFromUser(dropoffDist);
                    
                    return response;
                })
                .collect(Collectors.toList());

        log.info("Found {} matching routes", responses.size());
        return responses;
    }

    @Override
    @Transactional
    public FixedRouteResponse updateRouteStatus(Long routeId, Long driverId, String status) {
        FixedRoute route = fixedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        // Validate ownership
        if (!route.getDriver().getId().equals(driverId)) {
            throw new IllegalArgumentException("You don't have permission to update this route");
        }

        route.setStatus(FixedRoute.RouteStatus.valueOf(status));
        route = fixedRouteRepository.save(route);

        return fixedRouteMapper.toResponse(route);
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


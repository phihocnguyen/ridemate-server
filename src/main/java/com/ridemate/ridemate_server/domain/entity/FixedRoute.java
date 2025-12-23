package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

/**
 * Fixed Route entity - represents a recurring route created by a driver
 * with specific pickup/dropoff points and schedule
 */
@Getter
@Setter
@Entity
@Table(name = "fixed_routes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixedRoute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false, length = 200, columnDefinition = "VARCHAR(200)")
    private String routeName; // e.g., "Quận 1 → Thủ Đức"

    @Column(columnDefinition = "TEXT")
    private String description;

    // Pickup point
    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String pickupAddress;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    // Dropoff point
    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String dropoffAddress;

    @Column(nullable = false)
    private Double dropoffLatitude;

    @Column(nullable = false)
    private Double dropoffLongitude;

    // Schedule
    @Column(nullable = false)
    private LocalTime departureTime; // Scheduled departure time

    // Specific dates (comma-separated: "2025-12-22,2025-12-23,2025-12-24")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String specificDates; // Specific dates for this route

    // Pricing
    @Column(nullable = false)
    private Integer pricePerSeat; // Price in VND per passenger

    // Capacity
    @Column(nullable = false)
    private Integer totalSeats; // Total seats available

    @Column(nullable = false)
    @Builder.Default
    private Integer availableSeats = 0; // Current available seats

    // Distance in meters
    private Double distance;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RouteStatus status = RouteStatus.ACTIVE;

    // Proximity radius for matching (in meters)
    @Column(nullable = false)
    @Builder.Default
    private Integer pickupRadius = 500; // 500m default

    @Column(nullable = false)
    @Builder.Default
    private Integer dropoffRadius = 500; // 500m default

    public enum RouteStatus {
        ACTIVE,      // Route is active and accepting bookings
        INACTIVE,    // Temporarily inactive
        COMPLETED,   // Route completed
        CANCELLED    // Route cancelled
    }

    /**
     * Check if route is available on a specific date
     */
    public boolean isAvailableOnDate(String date) {
        return specificDates != null && specificDates.contains(date);
    }

    /**
     * Check if route has available seats
     */
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }

    /**
     * Decrease available seats when a booking is made
     */
    public void decreaseAvailableSeats() {
        if (availableSeats > 0) {
            availableSeats--;
        }
    }

    /**
     * Increase available seats when a booking is cancelled
     */
    public void increaseAvailableSeats() {
        if (availableSeats < totalSeats) {
            availableSeats++;
        }
    }
}


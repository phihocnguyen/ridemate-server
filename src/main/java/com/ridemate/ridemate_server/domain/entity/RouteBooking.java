package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * RouteBooking entity - represents a passenger's request to join a fixed route
 */
@Getter
@Setter
@Entity
@Table(name = "route_bookings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteBooking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private FixedRoute route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // Passenger's actual pickup location (might be slightly different from route's pickup)
    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String pickupAddress;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    // Passenger's actual dropoff location (might be slightly different from route's dropoff)
    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String dropoffAddress;

    @Column(nullable = false)
    private Double dropoffLatitude;

    @Column(nullable = false)
    private Double dropoffLongitude;

    // Booking details
    @Column(nullable = false)
    private LocalDate bookingDate; // Which date the passenger wants to join

    @Column(nullable = false)
    private Integer numberOfSeats; // Usually 1, but could be more

    @Column(nullable = false)
    private Integer totalPrice; // Price for this booking

    // Distance from passenger's location to route's pickup/dropoff points
    private Double pickupDistanceFromRoute; // in meters
    private Double dropoffDistanceFromRoute; // in meters

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Timestamps
    private LocalDateTime acceptedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    // Associated match (created when booking is accepted and trip starts)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    public enum BookingStatus {
        PENDING,      // Waiting for driver to accept
        ACCEPTED,     // Driver accepted the booking
        REJECTED,     // Driver rejected the booking
        CANCELLED,    // Passenger cancelled the booking
        IN_PROGRESS,  // Trip has started
        COMPLETED,    // Trip completed
        EXPIRED       // Booking expired (not accepted in time)
    }

    /**
     * Accept the booking
     */
    public void accept() {
        this.status = BookingStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * Reject the booking
     */
    public void reject() {
        this.status = BookingStatus.REJECTED;
        this.rejectedAt = LocalDateTime.now();
    }

    /**
     * Cancel the booking
     */
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Start the trip
     */
    public void startTrip() {
        this.status = BookingStatus.IN_PROGRESS;
    }

    /**
     * Complete the trip
     */
    public void complete() {
        this.status = BookingStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
}


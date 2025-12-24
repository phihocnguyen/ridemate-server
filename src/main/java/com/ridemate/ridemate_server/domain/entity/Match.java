package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "matches")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String pickupAddress;

    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String destinationAddress;

    // Coordinates for map routing (Optional but recommended)
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double destinationLatitude;
    private Double destinationLongitude;

    // Distance in meters
    private Double distance;

    // Duration in minutes (calculated when ride is completed)
    private Integer duration;

    // Coin cost calculated based on distance (km)
    // Formula: distance * COIN_PER_KM
    private Integer coin;

    // Actual fare amount in VND that passenger pays
    private Integer fare;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MatchStatus status = MatchStatus.WAITING;

    @OneToOne(mappedBy = "match", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Session session;

    // Store matched driver candidates as JSON for Supabase realtime broadcasting
    // This allows drivers to receive the full candidate list via realtime subscription
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "JSONB")
    private String matchedDriverCandidates;

    // Store route polyline (encoded polyline string from OSRM/Google Directions API)
    // This ensures both driver and passenger use the exact same route path
    @Column(columnDefinition = "TEXT")
    private String routePolyline;

    private LocalDateTime matchedAt;

    public enum MatchStatus {
        PENDING,     // No drivers available yet (queued)
        WAITING,     // Driver(s) found, waiting for acceptance
        ACCEPTED,    // Driver accepted the ride
        DRIVER_ARRIVED, // Driver arrived at pickup location
        IN_PROGRESS, // Ride started
        COMPLETED,   // Ride finished
        CANCELLED    // Cancelled by user or driver
    }
}
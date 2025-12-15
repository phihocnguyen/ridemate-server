package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(nullable = false, unique = true, length = 20, columnDefinition = "VARCHAR(20)")
    private String licensePlate;

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String make;

    @Column(nullable = false, length = 50, columnDefinition = "VARCHAR(50)")
    private String model;

    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private String color;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(nullable = false, length = 500, columnDefinition = "VARCHAR(500)")
    private String registrationDocumentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.PENDING;

    public enum VehicleType {
        MOTORBIKE, CAR, VAN, TRUCK
    }

    public enum VehicleStatus {
        PENDING,
        APPROVED,
        REJECTED,
        INACTIVE
    }
}


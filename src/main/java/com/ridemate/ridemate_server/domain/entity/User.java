package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = true, unique = true, length = 255)
    private String email;

    @Column(nullable = true)
    private String profilePictureUrl;

    @Column(nullable = false, columnDefinition = "FLOAT DEFAULT 0.0")
    @Builder.Default
    private Float rating = 0f;

    @Column(nullable = true, length = 500)
    private String faceIdData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;

    @Column(nullable = true)
    private Double currentLatitude;

    @Column(nullable = true)
    private Double currentLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = true, unique = true, length = 255)
    private String providerId;

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer coins = 0;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private Boolean isActive = true;

    public enum UserType {
        DRIVER, PASSENGER, ADMIN
    }

    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK
    }
}

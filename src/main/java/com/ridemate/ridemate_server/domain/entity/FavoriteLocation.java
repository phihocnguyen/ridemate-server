package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "favorite_locations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "VARCHAR(200)")
    private String name; // 

    @Column(nullable = false, columnDefinition = "VARCHAR(500)")
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;
}
package com.ridemate.ridemate_server.presentation.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUserDto {
    private Long userId;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private Integer coins;
    private Float rating;
    private String userType; // "DRIVER", "PASSENGER"
    private Integer totalTrips;
    private String membershipTier; // "Bronze", "Silver", "Gold", "Platinum"
}

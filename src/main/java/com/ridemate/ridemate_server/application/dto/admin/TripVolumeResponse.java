package com.ridemate.ridemate_server.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Trip volume statistics by date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripVolumeResponse {
    private LocalDate date;
    private Long tripCount;
    private Long totalCoin;
    private Long completedTrips;
    private Long cancelledTrips;
}

package com.ridemate.ridemate_server.application.dto.location;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Favorite location details")
public class LocationResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
}
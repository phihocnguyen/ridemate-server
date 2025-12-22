package com.ridemate.ridemate_server.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverApplicationRequest {
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    
    @NotBlank(message = "Vehicle info is required")
    private String vehicleInfo;
    
    private String licenseImageUrl;
    
    private String vehicleImageUrl;
}

package com.ridemate.ridemate_server.application.service.vehicle;

import com.ridemate.ridemate_server.application.dto.vehicle.RegisterVehicleRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.UpdateVehicleStatusRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.VehicleResponse;

import java.util.List;

public interface VehicleService {
    VehicleResponse registerVehicle(Long driverId, RegisterVehicleRequest request);
    VehicleResponse getVehicleById(Long vehicleId);
    List<VehicleResponse> getVehiclesByDriver(Long driverId);
    List<VehicleResponse> getPendingVehicles();
    VehicleResponse updateVehicleStatus(Long vehicleId, UpdateVehicleStatusRequest request);
    VehicleResponse getMyVehicle(Long driverId);
}


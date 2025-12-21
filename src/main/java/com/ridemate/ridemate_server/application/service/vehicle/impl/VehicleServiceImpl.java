package com.ridemate.ridemate_server.application.service.vehicle.impl;

import com.ridemate.ridemate_server.application.dto.vehicle.RegisterVehicleRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.UpdateVehicleStatusRequest;
import com.ridemate.ridemate_server.application.dto.vehicle.VehicleResponse;
import com.ridemate.ridemate_server.application.service.vehicle.VehicleService;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.Vehicle;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VehicleRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public VehicleResponse registerVehicle(Long driverId, RegisterVehicleRequest request) {
        // Validate driver exists
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        // ===== AUTO UPGRADE PASSENGER TO DRIVER =====
        if (driver.getUserType() == User.UserType.PASSENGER) {
            log.info("User {} is PASSENGER, auto-upgrading to DRIVER on first vehicle registration", driverId);
            driver.setUserType(User.UserType.DRIVER);
            // Auto set to ONLINE on first vehicle registration
            driver.setDriverStatus(User.DriverStatus.ONLINE);
            driver = userRepository.save(driver);
        } else if (driver.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("User must be a passenger or driver to register a vehicle");
        }

        // Check if license plate already exists
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already registered");
        }

        // Check if driver already has an approved vehicle
        List<Vehicle> existingVehicles = vehicleRepository.findByDriverIdAndStatus(
                driverId, Vehicle.VehicleStatus.APPROVED);
        if (!existingVehicles.isEmpty()) {
            throw new IllegalArgumentException("Driver already has an approved vehicle");
        }

        // Validate registration document URL
        if (request.getRegistrationDocumentUrl() == null || request.getRegistrationDocumentUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Registration document URL is required");
        }

        // Create vehicle entity
        Vehicle vehicle = Vehicle.builder()
                .driver(driver)
                .licensePlate(request.getLicensePlate())
                .make(request.getMake())
                .model(request.getModel())
                .color(request.getColor())
                .capacity(request.getCapacity())
                .vehicleType(Vehicle.VehicleType.valueOf(request.getVehicleType()))
                .registrationDocumentUrl(request.getRegistrationDocumentUrl())
                .status(Vehicle.VehicleStatus.PENDING)
                .build();

        vehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle registered successfully: {} by driver {}", vehicle.getLicensePlate(), driverId);

        return mapToResponse(vehicle);
    }

    @Override
    public VehicleResponse getVehicleById(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        return mapToResponse(vehicle);
    }

    @Override
    public List<VehicleResponse> getVehiclesByDriver(Long driverId) {
        List<Vehicle> vehicles = vehicleRepository.findByDriverId(driverId);
        return vehicles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VehicleResponse> getPendingVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.PENDING);
        return vehicles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicleStatus(Long vehicleId, UpdateVehicleStatusRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        Vehicle.VehicleStatus newStatus = Vehicle.VehicleStatus.valueOf(request.getStatus());
        
        // Validate status transition
        if (vehicle.getStatus() == Vehicle.VehicleStatus.APPROVED && newStatus == Vehicle.VehicleStatus.PENDING) {
            throw new IllegalArgumentException("Cannot change status from APPROVED to PENDING");
        }

        vehicle.setStatus(newStatus);
        vehicle = vehicleRepository.save(vehicle);
        
        log.info("Vehicle status updated: {} -> {} for vehicle {}", 
                vehicle.getStatus(), newStatus, vehicleId);

        return mapToResponse(vehicle);
    }

    @Override
    public VehicleResponse getMyVehicle(Long driverId) {
        List<Vehicle> vehicles = vehicleRepository.findByDriverId(driverId);
        if (vehicles.isEmpty()) {
            return null;
        }
        // Return the most recent vehicle
        Vehicle vehicle = vehicles.get(vehicles.size() - 1);
        return mapToResponse(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .vehicleId(vehicle.getId())
                .driverId(vehicle.getDriver().getId())
                .driverName(vehicle.getDriver().getFullName())
                .licensePlate(vehicle.getLicensePlate())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .capacity(vehicle.getCapacity())
                .vehicleType(vehicle.getVehicleType().toString())
                .registrationDocumentUrl(vehicle.getRegistrationDocumentUrl())
                .status(vehicle.getStatus().toString())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}


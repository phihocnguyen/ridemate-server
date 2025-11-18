package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Vehicle;
import com.ridemate.ridemate_server.domain.entity.Vehicle.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    List<Vehicle> findByDriverId(Long driverId);
    List<Vehicle> findByStatus(VehicleStatus status);
    boolean existsByLicensePlate(String licensePlate);
    List<Vehicle> findByDriverIdAndStatus(Long driverId, VehicleStatus status);
}


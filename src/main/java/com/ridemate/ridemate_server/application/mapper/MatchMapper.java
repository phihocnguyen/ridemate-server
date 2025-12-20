package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.domain.entity.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MatchMapper {

    @Mapping(target = "passengerId", source = "passenger.id")
    @Mapping(target = "passengerName", source = "passenger.fullName")
    @Mapping(target = "passengerPhone", source = "passenger.phoneNumber")
    @Mapping(target = "passengerAvatar", source = "passenger.profilePictureUrl")
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "driverPhone", source = "driver.phoneNumber")
    @Mapping(target = "driverAvatar", source = "driver.profilePictureUrl")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleInfo", source = "match", qualifiedByName = "mapVehicleInfo")
    @Mapping(target = "vehicleModel", source = "vehicle.model")
    @Mapping(target = "licensePlate", source = "vehicle.licensePlate")
    @Mapping(target = "status", source = "status")
    // Map coordinates for map routing
    @Mapping(target = "pickupLatitude", source = "pickupLatitude")
    @Mapping(target = "pickupLongitude", source = "pickupLongitude")
    @Mapping(target = "destinationLatitude", source = "destinationLatitude")
    @Mapping(target = "destinationLongitude", source = "destinationLongitude")
    // Ignore fields that are set programmatically
    @Mapping(target = "matchedDriverCandidates", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "passengerRating", ignore = true)
    @Mapping(target = "passengerReviews", ignore = true)
    @Mapping(target = "driverRating", ignore = true)
    @Mapping(target = "estimatedPrice", ignore = true)
    MatchResponse toResponse(Match match);

    @Named("mapVehicleInfo")
    default String mapVehicleInfo(Match match) {
        if (match.getVehicle() == null) return null;
        return match.getVehicle().getMake() + " " + 
               match.getVehicle().getModel() + " - " + 
               match.getVehicle().getLicensePlate();
    }
}
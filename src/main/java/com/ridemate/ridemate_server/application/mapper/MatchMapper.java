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
    @Mapping(target = "driverId", source = "driver.id")
    @Mapping(target = "driverName", source = "driver.fullName")
    @Mapping(target = "driverPhone", source = "driver.phoneNumber")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleInfo", source = "match", qualifiedByName = "mapVehicleInfo")
    @Mapping(target = "status", source = "status")
    MatchResponse toResponse(Match match);

    @Named("mapVehicleInfo")
    default String mapVehicleInfo(Match match) {
        if (match.getVehicle() == null) return null;
        return match.getVehicle().getMake() + " " + 
               match.getVehicle().getModel() + " - " + 
               match.getVehicle().getLicensePlate();
    }
}
package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.route.FixedRouteResponse;
import com.ridemate.ridemate_server.domain.entity.FixedRoute;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FixedRouteMapper {

    @Mapping(source = "driver.id", target = "driverId")
    @Mapping(source = "driver.fullName", target = "driverName")
    @Mapping(source = "driver.phoneNumber", target = "driverPhone")
    @Mapping(source = "driver.profilePictureUrl", target = "driverAvatar")
    @Mapping(source = "driver.rating", target = "driverRating")
    @Mapping(source = "vehicle.id", target = "vehicleId")
    @Mapping(source = "vehicle.model", target = "vehicleModel")
    @Mapping(source = "vehicle.licensePlate", target = "licensePlate")
    @Mapping(target = "vehicleInfo", expression = "java(route.getVehicle().getModel() + \" - \" + route.getVehicle().getLicensePlate())")
    @Mapping(target = "status", expression = "java(route.getStatus().name())")
    @Mapping(source = "specificDates", target = "specificDates")
    @Mapping(target = "pickupDistanceFromUser", ignore = true)
    @Mapping(target = "dropoffDistanceFromUser", ignore = true)
    FixedRouteResponse toResponse(FixedRoute route);
}


package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.route.RouteBookingResponse;
import com.ridemate.ridemate_server.domain.entity.RouteBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteBookingMapper {

    @Mapping(source = "route.id", target = "routeId")
    @Mapping(source = "route.routeName", target = "routeName")
    @Mapping(source = "route.pickupAddress", target = "routePickupAddress")
    @Mapping(source = "route.dropoffAddress", target = "routeDropoffAddress")
    @Mapping(target = "routeDepartureTime", expression = "java(booking.getRoute().getDepartureTime().toString())")
    @Mapping(source = "passenger.id", target = "passengerId")
    @Mapping(source = "passenger.fullName", target = "passengerName")
    @Mapping(source = "passenger.phoneNumber", target = "passengerPhone")
    @Mapping(source = "passenger.profilePictureUrl", target = "passengerAvatar")
    @Mapping(source = "passenger.rating", target = "passengerRating")
    @Mapping(source = "route.driver.id", target = "driverId")
    @Mapping(source = "route.driver.fullName", target = "driverName")
    @Mapping(source = "route.driver.phoneNumber", target = "driverPhone")
    @Mapping(target = "vehicleInfo", expression = "java(booking.getRoute().getVehicle().getModel() + \" - \" + booking.getRoute().getVehicle().getLicensePlate())")
    @Mapping(source = "match.id", target = "matchId")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    RouteBookingResponse toResponse(RouteBooking booking);
}


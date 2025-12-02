package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.location.LocationResponse;
import com.ridemate.ridemate_server.domain.entity.FavoriteLocation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationResponse toResponse(FavoriteLocation location);
}
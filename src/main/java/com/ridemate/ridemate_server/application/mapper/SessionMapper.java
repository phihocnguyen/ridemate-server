package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.session.SessionResponse;
import com.ridemate.ridemate_server.domain.entity.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SessionMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "matchId", source = "match.id")
    SessionResponse toResponse(Session session);
}

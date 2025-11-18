package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.user.UserDto;
import com.ridemate.ridemate_server.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userType", source = "userType")
    @Mapping(target = "authProvider", source = "authProvider")
    UserDto toUserDto(User user);
}
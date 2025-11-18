package com.ridemate.ridemate_server.application.service.user;

import com.ridemate.ridemate_server.application.dto.user.UserDto;

public interface UserService {
    UserDto getUserProfile(Long userId);
}
package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.notification.NotificationResponse;
import com.ridemate.ridemate_server.domain.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
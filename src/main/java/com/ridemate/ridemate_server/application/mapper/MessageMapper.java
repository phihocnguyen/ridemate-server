package com.ridemate.ridemate_server.application.mapper;

import com.ridemate.ridemate_server.application.dto.chat.MessageResponse;
import com.ridemate.ridemate_server.domain.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.fullName")
    @Mapping(target = "senderAvatar", source = "sender.profilePictureUrl")
    @Mapping(target = "timestamp", source = "createdAt")
    MessageResponse toResponse(Message message);
}
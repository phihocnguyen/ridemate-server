package com.ridemate.ridemate_server.application.service.chat;

import com.ridemate.ridemate_server.application.dto.chat.MessageResponse;
import com.ridemate.ridemate_server.application.dto.chat.SendMessageRequest;
import java.util.List;

public interface ChatService {
    MessageResponse sendMessage(Long senderId, SendMessageRequest request);
    List<MessageResponse> getSessionMessages(Long sessionId, Long userId);
}
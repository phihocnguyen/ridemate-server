package com.ridemate.ridemate_server.application.service.message;

import com.ridemate.ridemate_server.application.dto.message.CreateMessageRequest;
import com.ridemate.ridemate_server.application.dto.message.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {

    MessageResponse createMessage(CreateMessageRequest request);

    List<MessageResponse> getMessagesBySession(Long sessionId);

    Page<MessageResponse> getMessagesBySessionPaginated(Long sessionId, Pageable pageable);

    List<MessageResponse> getUnreadMessages(Long sessionId, Long userId);

    MessageResponse markMessageAsRead(Long messageId);

    void markAllMessagesAsRead(Long sessionId, Long userId);

    MessageResponse getMessageById(Long messageId);

    long getUnreadMessageCount(Long sessionId, Long userId);
}

package com.ridemate.ridemate_server.application.service.chat.impl;

import com.ridemate.ridemate_server.application.dto.chat.MessageResponse;
import com.ridemate.ridemate_server.application.dto.chat.SendMessageRequest;
import com.ridemate.ridemate_server.application.mapper.MessageMapper;
import com.ridemate.ridemate_server.application.service.chat.ChatService;
import com.ridemate.ridemate_server.domain.entity.Message;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MessageRepository;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getIsActive()) {
            throw new IllegalArgumentException("Cannot send message to an inactive session");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check permission (Only Passenger or Driver of this match can send messages)
        boolean isAuthorized = session.getMatch().getPassenger().getId().equals(senderId) ||
                               (session.getMatch().getDriver() != null && 
                                session.getMatch().getDriver().getId().equals(senderId));

        if (!isAuthorized) {
            throw new IllegalArgumentException("You are not a participant of this session");
        }

        Message message = Message.builder()
                .session(session)
                .sender(sender)
                .content(request.getContent())
                .type(Message.MessageType.valueOf(request.getType()))
                .build();

        message = messageRepository.save(message);
        return messageMapper.toResponse(message);
    }

    @Override
    public List<MessageResponse> getSessionMessages(Long sessionId, Long userId) {
        // Simple validation to ensure user has access to history
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        boolean isAuthorized = session.getMatch().getPassenger().getId().equals(userId) ||
                               (session.getMatch().getDriver() != null && 
                                session.getMatch().getDriver().getId().equals(userId));

        if (!isAuthorized) {
             throw new IllegalArgumentException("Unauthorized to view this chat history");
        }

        List<Message> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messages.stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
    }
}
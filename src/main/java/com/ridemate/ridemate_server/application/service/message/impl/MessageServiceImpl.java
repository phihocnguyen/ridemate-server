package com.ridemate.ridemate_server.application.service.message.impl;

import com.ridemate.ridemate_server.application.dto.message.CreateMessageRequest;
import com.ridemate.ridemate_server.application.dto.message.MessageResponse;
import com.ridemate.ridemate_server.application.mapper.MessageMapper;
import com.ridemate.ridemate_server.application.service.message.MessageService;
import com.ridemate.ridemate_server.domain.entity.Message;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MessageRepository;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

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
    public MessageResponse createMessage(CreateMessageRequest request) {
        Long currentUserId = getCurrentUserId();

        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + request.getSessionId()));

        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + currentUserId));

        Message.MessageType messageType;
        try {
            messageType = Message.MessageType.valueOf(request.getMessageType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid message type: " + request.getMessageType());
        }

        Message message = Message.builder()
                .session(session)
                .sender(sender)
                .messageContent(request.getMessageContent())
                .timestamp(LocalDateTime.now())
                .messageType(messageType)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);
        return messageMapper.toMessageResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessagesBySession(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }

        List<Message> messages = messageRepository.findBySessionIdOrderByTimestampDesc(sessionId);
        return messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessagesBySessionPaginated(Long sessionId, Pageable pageable) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }

        return messageRepository.findBySessionIdOrderByTimestampDesc(sessionId, pageable)
                .map(messageMapper::toMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getUnreadMessages(Long sessionId, Long userId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Message> unreadMessages = messageRepository.findUnreadMessagesBySessionAndUser(sessionId, userId);
        return unreadMessages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageResponse markMessageAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        message.setIsRead(true);
        Message updatedMessage = messageRepository.save(message);
        return messageMapper.toMessageResponse(updatedMessage);
    }

    @Override
    @Transactional
    public void markAllMessagesAsRead(Long sessionId, Long userId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Message> unreadMessages = messageRepository.findUnreadMessagesBySessionAndUser(sessionId, userId);
        for (Message message : unreadMessages) {
            message.setIsRead(true);
        }
        messageRepository.saveAll(unreadMessages);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));

        return messageMapper.toMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(Long sessionId, Long userId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found with id: " + sessionId);
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return messageRepository.findUnreadMessagesBySessionAndUser(sessionId, userId).size();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getPrincipal();
    }
}

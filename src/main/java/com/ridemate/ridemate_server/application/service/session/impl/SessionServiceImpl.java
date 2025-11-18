package com.ridemate.ridemate_server.application.service.session.impl;

import com.ridemate.ridemate_server.application.dto.session.SessionResponse;
import com.ridemate.ridemate_server.application.mapper.SessionMapper;
import com.ridemate.ridemate_server.application.service.session.SessionService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Session;
import com.ridemate.ridemate_server.domain.repository.SessionRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionMapper sessionMapper;

    @Override
    @Transactional
    public SessionResponse createSession(Match match) {
        // Create a new session when match is created
        Session session = Session.builder()
                .match(match)
                .startTime(LocalDateTime.now())
                .isActive(true)
                .build();

        session = sessionRepository.save(session);
        return sessionMapper.toResponse(session);
    }

    @Override
    public SessionResponse getSessionByMatchId(Long matchId) {
        Session session = sessionRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for match ID: " + matchId));
        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional
    public SessionResponse updateSessionStatus(Long matchId, Boolean isActive) {
        Session session = sessionRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for match ID: " + matchId));

        session.setIsActive(isActive);
        
        if (!isActive) {
            session.setEndTime(LocalDateTime.now());
        }

        session = sessionRepository.save(session);
        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional
    public SessionResponse endSession(Long matchId) {
        Session session = sessionRepository.findByMatchId(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found for match ID: " + matchId));

        session.setEndTime(LocalDateTime.now());
        session.setIsActive(false);

        session = sessionRepository.save(session);
        return sessionMapper.toResponse(session);
    }
}

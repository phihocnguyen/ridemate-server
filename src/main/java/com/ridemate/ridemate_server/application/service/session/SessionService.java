package com.ridemate.ridemate_server.application.service.session;

import com.ridemate.ridemate_server.application.dto.session.SessionResponse;
import com.ridemate.ridemate_server.domain.entity.Match;

public interface SessionService {
    /**
     * Create a new session when a match is successfully created
     * @param match the Match entity
     * @return SessionResponse
     */
    SessionResponse createSession(Match match);

    /**
     * Get session details by match id
     * @param matchId the match id
     * @return SessionResponse
     */
    SessionResponse getSessionByMatchId(Long matchId);

    /**
     * Update session status
     * @param matchId the match id
     * @param isActive session active status
     * @return updated SessionResponse
     */
    SessionResponse updateSessionStatus(Long matchId, Boolean isActive);

    /**
     * End a session (set end_time and is_active to false)
     * @param matchId the match id
     * @return updated SessionResponse
     */
    SessionResponse endSession(Long matchId);
}

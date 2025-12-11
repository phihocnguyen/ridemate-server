package com.ridemate.ridemate_server.application.service.match;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.dto.match.UpdateMatchStatusRequest;
import java.util.List;

public interface MatchService {
    MatchResponse bookRide(Long passengerId, BookRideRequest request);
    MatchResponse getMatchById(Long matchId);
    List<MatchResponse> getMyHistory(Long userId);
    MatchResponse acceptRide(Long matchId, Long driverId);
    MatchResponse updateMatchStatus(Long matchId, Long userId, UpdateMatchStatusRequest request);
    MatchResponse cancelMatch(Long matchId, Long userId);
    List<MatchResponse> getWaitingMatches();
}
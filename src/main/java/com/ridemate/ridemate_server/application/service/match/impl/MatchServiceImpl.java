package com.ridemate.ridemate_server.application.service.match.impl;

import com.ridemate.ridemate_server.application.dto.match.BookRideRequest;
import com.ridemate.ridemate_server.application.dto.match.MatchResponse;
import com.ridemate.ridemate_server.application.mapper.MatchMapper;
import com.ridemate.ridemate_server.application.service.match.MatchService;
import com.ridemate.ridemate_server.application.service.session.SessionService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchMapper matchMapper;

    @Autowired
    private SessionService sessionService;

    @Override
    @Transactional
    public MatchResponse bookRide(Long passengerId, BookRideRequest request) {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Basic validation
        if (passenger.getUserType() == User.UserType.DRIVER) {
             // In some apps drivers can book rides too, but logic depends on your requirement
        }

        Match match = Match.builder()
                .passenger(passenger)
                .pickupAddress(request.getPickupAddress())
                .destinationAddress(request.getDestinationAddress())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .destinationLatitude(request.getDestinationLatitude())
                .destinationLongitude(request.getDestinationLongitude())
                .fare(request.getFare())
                .status(Match.MatchStatus.WAITING)
                .build();

        match = matchRepository.save(match);
        
        // Automatically create session when match is created
        sessionService.createSession(match);
        
        // TODO: Trigger notification/socket to nearby drivers here
        
        return matchMapper.toResponse(match);
    }

    @Override
    public MatchResponse getMatchById(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        return matchMapper.toResponse(match);
    }

    @Override
    public List<MatchResponse> getMyHistory(Long userId) {
        // This logic might need improvement to handle both passenger and driver roles
        // For now assuming passenger history
        List<Match> matches = matchRepository.findByPassengerId(userId);
        return matches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }
}
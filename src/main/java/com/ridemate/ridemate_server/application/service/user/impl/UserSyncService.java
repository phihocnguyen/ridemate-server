package com.ridemate.ridemate_server.application.service.user.impl;

import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to sync user statistics with actual match data
 * This fixes data inconsistencies between User.totalRidesCompleted and actual COMPLETED matches
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    /**
     * Sync totalRidesCompleted for a specific user
     * Counts all COMPLETED matches where user is the driver
     */
    @Transactional
    public void syncUserRideStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Count actual completed rides as driver
        List<Match> completedMatches = matchRepository.findByDriverIdAndStatus(
                userId, 
                Match.MatchStatus.COMPLETED
        );
        
        int actualCompletedRides = completedMatches.size();
        int oldValue = user.getTotalRidesCompleted();

        if (actualCompletedRides != oldValue) {
            user.setTotalRidesCompleted(actualCompletedRides);
            
            // Recalculate completion rate
            if (user.getTotalRidesAccepted() > 0) {
                float completionRate = ((float) actualCompletedRides / user.getTotalRidesAccepted()) * 100;
                user.setCompletionRate(completionRate);
            }
            
            userRepository.save(user);
            
            log.info("✅ Synced user {} rides: {} -> {} (diff: {})", 
                    userId, oldValue, actualCompletedRides, actualCompletedRides - oldValue);
        } else {
            log.info("✓ User {} rides already in sync: {}", userId, actualCompletedRides);
        }
    }

    /**
     * Sync all users' ride statistics
     * Use with caution - can be slow for large databases
     */
    @Transactional
    public void syncAllUsersRideStats() {
        log.info("🔄 Starting sync for all users...");
        
        List<User> allUsers = userRepository.findAll();
        int syncedCount = 0;
        int errorCount = 0;

        for (User user : allUsers) {
            try {
                syncUserRideStats(user.getId());
                syncedCount++;
            } catch (Exception e) {
                log.error("❌ Failed to sync user {}: {}", user.getId(), e.getMessage());
                errorCount++;
            }
        }

        log.info("✅ Sync completed: {} users synced, {} errors", syncedCount, errorCount);
    }
}


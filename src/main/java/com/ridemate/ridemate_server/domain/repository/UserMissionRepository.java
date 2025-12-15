package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Mission;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    
    // Find user mission by user and mission
    Optional<UserMission> findByUserAndMission(User user, Mission mission);
    
    // Find all missions for a user
    List<UserMission> findByUser(User user);
    
    // Find all missions for a user by user ID
    List<UserMission> findByUserId(Long userId);
    
    // Find completed missions for a user
    List<UserMission> findByUserAndIsCompletedTrue(User user);
    
    // Find incomplete missions for a user
    List<UserMission> findByUserAndIsCompletedFalse(User user);
    
    // Find missions where reward not claimed
    List<UserMission> findByUserAndIsCompletedTrueAndRewardClaimedFalse(User user);
    
    // Find user mission by user ID and mission ID
    @Query("SELECT um FROM UserMission um WHERE um.user.id = :userId AND um.mission.id = :missionId")
    Optional<UserMission> findByUserIdAndMissionId(@Param("userId") Long userId, 
                                                     @Param("missionId") Long missionId);
    
    // Count completed missions for a user
    Long countByUserAndIsCompletedTrue(User user);
    
    // Count unclaimed rewards for a user
    Long countByUserAndIsCompletedTrueAndRewardClaimedFalse(User user);
    
    // Check if user already has this mission
    boolean existsByUserAndMission(User user, Mission mission);
    
    // Get user missions with active missions only
    @Query("SELECT um FROM UserMission um WHERE um.user.id = :userId " +
           "AND um.mission.isActive = true ORDER BY um.createdAt DESC")
    List<UserMission> findActiveUserMissions(@Param("userId") Long userId);
}

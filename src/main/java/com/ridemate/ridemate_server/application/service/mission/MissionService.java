package com.ridemate.ridemate_server.application.service.mission;

import com.ridemate.ridemate_server.domain.entity.Mission;
import com.ridemate.ridemate_server.presentation.dto.mission.CreateMissionRequest;
import com.ridemate.ridemate_server.presentation.dto.mission.MissionDto;
import com.ridemate.ridemate_server.presentation.dto.mission.UpdateMissionRequest;
import com.ridemate.ridemate_server.presentation.dto.mission.UserMissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MissionService {
    
    // Admin operations
    MissionDto createMission(CreateMissionRequest request);
    
    MissionDto updateMission(Long missionId, UpdateMissionRequest request);
    
    void deleteMission(Long missionId);
    
    MissionDto getMissionById(Long missionId);
    
    Page<MissionDto> getAllMissions(Pageable pageable);
    
    Page<MissionDto> getActiveMissions(Pageable pageable);
    
    Page<MissionDto> getMissionsByType(Mission.MissionType type, Pageable pageable);
    
    // User operations
    List<UserMissionDto> getUserMissions(Long userId);
    
    List<MissionDto> getAvailableMissionsForUser(Long userId);
    
    UserMissionDto acceptMission(Long userId, Long missionId);
    
    UserMissionDto updateProgress(Long userId, Long missionId, int progressAmount);
    
    UserMissionDto claimReward(Long userId, Long missionId);
    
    // Statistics
    Long getActiveMissionCount();
    
    Long getUserCompletedMissionCount(Long userId);
    
    Long getUserUnclaimedRewardCount(Long userId);
}

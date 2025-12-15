package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.service.mission.MissionService;
import com.ridemate.ridemate_server.presentation.dto.mission.MissionDto;
import com.ridemate.ridemate_server.presentation.dto.mission.UserMissionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "Mission", description = "Mission management APIs for users")
public class MissionController {
    
    private final MissionService missionService;
    
    @GetMapping("/available")
    @Operation(summary = "Get available missions for current user")
    public ResponseEntity<List<MissionDto>> getAvailableMissions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<MissionDto> missions = missionService.getAvailableMissionsForUser(userId);
        return ResponseEntity.ok(missions);
    }
    
    @GetMapping("/my-missions")
    @Operation(summary = "Get all missions for current user")
    public ResponseEntity<List<UserMissionDto>> getMyMissions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<UserMissionDto> userMissions = missionService.getUserMissions(userId);
        return ResponseEntity.ok(userMissions);
    }
    
    @PostMapping("/{missionId}/accept")
    @Operation(summary = "Accept a mission")
    public ResponseEntity<UserMissionDto> acceptMission(
            @PathVariable Long missionId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserMissionDto userMission = missionService.acceptMission(userId, missionId);
        return ResponseEntity.ok(userMission);
    }
    
    @PostMapping("/{missionId}/claim")
    @Operation(summary = "Claim mission reward")
    public ResponseEntity<UserMissionDto> claimReward(
            @PathVariable Long missionId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserMissionDto userMission = missionService.claimReward(userId, missionId);
        return ResponseEntity.ok(userMission);
    }
    
    @GetMapping("/stats")
    @Operation(summary = "Get mission statistics for current user")
    public ResponseEntity<Map<String, Long>> getMissionStats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long completedCount = missionService.getUserCompletedMissionCount(userId);
        Long unclaimedCount = missionService.getUserUnclaimedRewardCount(userId);
        
        return ResponseEntity.ok(Map.of(
                "completed", completedCount,
                "unclaimedRewards", unclaimedCount
        ));
    }
}

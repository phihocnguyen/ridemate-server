package com.ridemate.ridemate_server.application.service.mission.impl;

import com.ridemate.ridemate_server.application.service.mission.MissionService;
import com.ridemate.ridemate_server.domain.entity.Mission;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.UserMission;
import com.ridemate.ridemate_server.domain.entity.UserVoucher;
import com.ridemate.ridemate_server.domain.entity.Voucher;
import com.ridemate.ridemate_server.domain.repository.MissionRepository;
import com.ridemate.ridemate_server.domain.repository.UserMissionRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.UserVoucherRepository;
import com.ridemate.ridemate_server.domain.repository.VoucherRepository;
import com.ridemate.ridemate_server.presentation.dto.mission.CreateMissionRequest;
import com.ridemate.ridemate_server.presentation.dto.mission.MissionDto;
import com.ridemate.ridemate_server.presentation.dto.mission.UpdateMissionRequest;
import com.ridemate.ridemate_server.presentation.dto.mission.UserMissionDto;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {
    
    private final MissionRepository missionRepository;
    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    
    @Override
    @Transactional
    public MissionDto createMission(CreateMissionRequest request) {
        Mission mission = Mission.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .missionType(request.getMissionType())
                .targetType(request.getTargetType())
                .targetValue(request.getTargetValue())
                .rewardPoints(request.getRewardPoints())
                .rewardVoucherId(request.getRewardVoucherId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive())
                .priority(request.getPriority())
                .iconUrl(request.getIconUrl())
                .bannerUrl(request.getBannerUrl())
                .build();
        
        mission = missionRepository.save(mission);
        return convertToDto(mission);
    }
    
    @Override
    @Transactional
    public MissionDto updateMission(Long missionId, UpdateMissionRequest request) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        
        if (request.getTitle() != null) {
            mission.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            mission.setDescription(request.getDescription());
        }
        if (request.getMissionType() != null) {
            mission.setMissionType(request.getMissionType());
        }
        if (request.getTargetType() != null) {
            mission.setTargetType(request.getTargetType());
        }
        if (request.getTargetValue() != null) {
            mission.setTargetValue(request.getTargetValue());
        }
        if (request.getRewardPoints() != null) {
            mission.setRewardPoints(request.getRewardPoints());
        }
        if (request.getRewardVoucherId() != null) {
            mission.setRewardVoucherId(request.getRewardVoucherId());
        }
        if (request.getStartDate() != null) {
            mission.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            mission.setEndDate(request.getEndDate());
        }
        if (request.getIsActive() != null) {
            mission.setIsActive(request.getIsActive());
        }
        if (request.getPriority() != null) {
            mission.setPriority(request.getPriority());
        }
        if (request.getIconUrl() != null) {
            mission.setIconUrl(request.getIconUrl());
        }
        if (request.getBannerUrl() != null) {
            mission.setBannerUrl(request.getBannerUrl());
        }
        
        mission = missionRepository.save(mission);
        return convertToDto(mission);
    }
    
    @Override
    @Transactional
    public void deleteMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        missionRepository.delete(mission);
    }
    
    @Override
    @Transactional(readOnly = true)
    public MissionDto getMissionById(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        return convertToDto(mission);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getAllMissions(Pageable pageable) {
        return missionRepository.findAll(pageable).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getActiveMissions(Pageable pageable) {
        return missionRepository.findByIsActiveTrue(pageable).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<MissionDto> getMissionsByType(Mission.MissionType type, Pageable pageable) {
        return missionRepository.findByMissionType(type, pageable).map(this::convertToDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserMissionDto> getUserMissions(Long userId) {
        List<UserMission> userMissions = userMissionRepository.findByUserId(userId);
        return userMissions.stream()
                .map(this::convertToUserMissionDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MissionDto> getAvailableMissionsForUser(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Mission> availableMissions = missionRepository.findAvailableMissions(now);
        
        // Filter out missions user already accepted
        List<UserMission> userMissions = userMissionRepository.findByUserId(userId);
        List<Long> acceptedMissionIds = userMissions.stream()
                .map(um -> um.getMission().getId())
                .collect(Collectors.toList());
        
        return availableMissions.stream()
                .filter(mission -> !acceptedMissionIds.contains(mission.getId()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public UserMissionDto acceptMission(Long userId, Long missionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ResourceNotFoundException("Mission not found with id: " + missionId));
        
        // Check if user already accepted this mission
        if (userMissionRepository.existsByUserAndMission(user, mission)) {
            throw new IllegalStateException("User has already accepted this mission");
        }
        
        // Check if mission is available
        if (!mission.isAvailable()) {
            throw new IllegalStateException("Mission is not available");
        }
        
        UserMission userMission = UserMission.builder()
                .user(user)
                .mission(mission)
                .progress(0)
                .isCompleted(false)
                .rewardClaimed(false)
                .expiresAt(mission.getEndDate())
                .build();
        
        userMission = userMissionRepository.save(userMission);
        return convertToUserMissionDto(userMission);
    }
    
    @Override
    @Transactional
    public UserMissionDto updateProgress(Long userId, Long missionId, int progressAmount) {
        UserMission userMission = userMissionRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserMission not found for user: " + userId + " and mission: " + missionId));
        
        if (userMission.getIsCompleted()) {
            throw new IllegalStateException("Mission is already completed");
        }
        
        userMission.incrementProgress(progressAmount);
        userMission = userMissionRepository.save(userMission);
        
        return convertToUserMissionDto(userMission);
    }
    
    @Override
    @Transactional
    public UserMissionDto claimReward(Long userId, Long missionId) {
        UserMission userMission = userMissionRepository.findByUserIdAndMissionId(userId, missionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "UserMission not found for user: " + userId + " and mission: " + missionId));
        
        if (!userMission.canClaim()) {
            throw new IllegalStateException("Cannot claim reward: mission not completed or already claimed");
        }
        
        userMission.claimReward();
        
        // Add reward points to user's account
        User user = userMission.getUser();
        Mission mission = userMission.getMission();
        int rewardPoints = mission.getRewardPoints() != null ? mission.getRewardPoints() : 0;
        
        if (rewardPoints > 0) {
            int currentCoins = user.getCoins() != null ? user.getCoins() : 0;
            user.setCoins(currentCoins + rewardPoints);
            userRepository.save(user);
        }
        
        // If rewardVoucherId is present, create UserVoucher
        if (mission.getRewardVoucherId() != null) {
            Voucher voucher = voucherRepository.findById(mission.getRewardVoucherId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Voucher not found with id: " + mission.getRewardVoucherId()));
            
            // Check if voucher is active and not expired
            if (!voucher.getIsActive()) {
                throw new IllegalStateException("Voucher is not active");
            }
            
            if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Voucher has expired");
            }
            
            // Create UserVoucher
            UserVoucher userVoucher = UserVoucher.builder()
                    .user(user)
                    .voucher(voucher)
                    .status(UserVoucher.UserVoucherStatus.UNUSED)
                    .acquiredDate(LocalDateTime.now())
                    .build();
            
            userVoucherRepository.save(userVoucher);
        }
        
        userMission = userMissionRepository.save(userMission);
        return convertToUserMissionDto(userMission);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getActiveMissionCount() {
        return missionRepository.countByIsActiveTrue();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUserCompletedMissionCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMissionRepository.countByUserAndIsCompletedTrue(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUserUnclaimedRewardCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMissionRepository.countByUserAndIsCompletedTrueAndRewardClaimedFalse(user);
    }
    
    private MissionDto convertToDto(Mission mission) {
        return MissionDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .missionType(mission.getMissionType())
                .targetType(mission.getTargetType())
                .targetValue(mission.getTargetValue())
                .rewardPoints(mission.getRewardPoints())
                .rewardVoucherId(mission.getRewardVoucherId())
                .startDate(mission.getStartDate())
                .endDate(mission.getEndDate())
                .isActive(mission.getIsActive())
                .priority(mission.getPriority())
                .iconUrl(mission.getIconUrl())
                .bannerUrl(mission.getBannerUrl())
                .isExpired(mission.isExpired())
                .isAvailable(mission.isAvailable())
                .createdAt(mission.getCreatedAt())
                .updatedAt(mission.getUpdatedAt())
                .build();
    }
    
    private UserMissionDto convertToUserMissionDto(UserMission userMission) {
        return UserMissionDto.builder()
                .id(userMission.getId())
                .userId(userMission.getUser().getId())
                .mission(convertToDto(userMission.getMission()))
                .progress(userMission.getProgress())
                .progressPercentage(userMission.getProgressPercentage())
                .isCompleted(userMission.getIsCompleted())
                .completedAt(userMission.getCompletedAt())
                .rewardClaimed(userMission.getRewardClaimed())
                .claimedAt(userMission.getClaimedAt())
                .expiresAt(userMission.getExpiresAt())
                .canClaim(userMission.canClaim())
                .build();
    }
}

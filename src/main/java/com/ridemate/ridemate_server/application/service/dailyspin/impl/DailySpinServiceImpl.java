package com.ridemate.ridemate_server.application.service.dailyspin.impl;

import com.ridemate.ridemate_server.application.dto.dailyspin.DailySpinResponse;
import com.ridemate.ridemate_server.application.service.dailyspin.DailySpinService;
import com.ridemate.ridemate_server.domain.entity.DailySpin;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.DailySpinRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailySpinServiceImpl implements DailySpinService {

    private final DailySpinRepository dailySpinRepository;
    private final UserRepository userRepository;
    private static final int[] REWARDS = {100, 200, 300, 400, 500}; // Possible rewards
    private final Random random = new Random();

    @Override
    public DailySpinResponse checkDailySpin(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<DailySpin> todaySpin = dailySpinRepository.findByUserIdAndSpinDate(userId, today);
        
        boolean canSpin = todaySpin.isEmpty();
        
        log.info("Checking daily spin for user {} on date {}: canSpin = {}", userId, today, canSpin);
        
        if (todaySpin.isPresent()) {
            DailySpin spin = todaySpin.get();
            log.info("Found existing spin: rewardPoints = {}, spinTime = {}", 
                    spin.getRewardPoints(), spin.getSpinTime());
            return DailySpinResponse.builder()
                    .id(spin.getId())
                    .userId(userId)
                    .spinDate(spin.getSpinDate())
                    .rewardPoints(spin.getRewardPoints())
                    .spinTime(spin.getSpinTime())
                    .canSpinToday(false)
                    .build();
        }
        
        log.info("No existing spin found - user can spin today");
        return DailySpinResponse.builder()
                .userId(userId)
                .spinDate(today)
                .canSpinToday(true)
                .build();
    }

    @Override
    @Transactional
    public DailySpinResponse performSpin(Long userId) {
        LocalDate today = LocalDate.now();
        
        // Check if already spun today
        if (dailySpinRepository.existsByUserIdAndSpinDate(userId, today)) {
            throw new IllegalStateException("Bạn đã quay hôm nay rồi! Vui lòng quay lại vào ngày mai.");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Random reward (100, 200, 300, 400, or 500)
        int rewardIndex = random.nextInt(REWARDS.length);
        int rewardPoints = REWARDS[rewardIndex];

        // Create daily spin record
        DailySpin dailySpin = DailySpin.builder()
                .user(user)
                .spinDate(today)
                .rewardPoints(rewardPoints)
                .spinTime(LocalDateTime.now())
                .notes("Daily spin reward")
                .build();

        dailySpin = dailySpinRepository.save(dailySpin);

        // Add points to user
        int currentCoins = user.getCoins() != null ? user.getCoins() : 0;
        user.setCoins(currentCoins + rewardPoints);
        userRepository.save(user);

        log.info("Daily spin completed for user {}: {} points", userId, rewardPoints);

        return DailySpinResponse.builder()
                .id(dailySpin.getId())
                .userId(userId)
                .spinDate(today)
                .rewardPoints(rewardPoints)
                .spinTime(dailySpin.getSpinTime())
                .canSpinToday(false)
                .build();
    }
}


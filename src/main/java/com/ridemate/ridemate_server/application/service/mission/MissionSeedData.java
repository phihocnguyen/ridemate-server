package com.ridemate.ridemate_server.application.service.mission;

import com.ridemate.ridemate_server.domain.entity.Mission;
import com.ridemate.ridemate_server.domain.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Seed data for Missions
 * This will run only in development/test profile
 * To use: add --spring.profiles.active=dev to your application.properties or run command
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class MissionSeedData {

    private final MissionRepository missionRepository;

    @Bean
    CommandLineRunner initMissionData() {
        return args -> {
            if (missionRepository.count() > 0) {
                log.info("Mission data already exists, skipping seed data");
                return;
            }

            log.info("Seeding missions...");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = now.plusDays(30);

            List<Mission> missions = Arrays.asList(
                // 1. Daily Mission: Complete 1 trip
                Mission.builder()
                    .title("Hoàn thành chuyến đi đầu tiên")
                    .description("Hoàn thành 1 chuyến đi để nhận điểm thưởng")
                    .missionType(Mission.MissionType.DAILY)
                    .targetType(Mission.TargetType.COMPLETE_TRIPS)
                    .targetValue(1)
                    .rewardPoints(50)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(1)
                    .build(),

                // 2. Daily Mission: Complete 3 trips
                Mission.builder()
                    .title("Hoàn thành 3 chuyến đi")
                    .description("Hoàn thành 3 chuyến đi trong ngày để nhận điểm thưởng")
                    .missionType(Mission.MissionType.DAILY)
                    .targetType(Mission.TargetType.COMPLETE_TRIPS)
                    .targetValue(3)
                    .rewardPoints(150)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(2)
                    .build(),

                // 3. Weekly Mission: Complete 10 trips
                Mission.builder()
                    .title("Hoàn thành 10 chuyến đi trong tuần")
                    .description("Hoàn thành 10 chuyến đi trong tuần để nhận điểm thưởng lớn")
                    .missionType(Mission.MissionType.WEEKLY)
                    .targetType(Mission.TargetType.COMPLETE_TRIPS)
                    .targetValue(10)
                    .rewardPoints(500)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(3)
                    .build(),

                // 4. Special Mission: Complete profile
                Mission.builder()
                    .title("Hoàn thiện hồ sơ")
                    .description("Cập nhật đầy đủ thông tin hồ sơ để nhận điểm thưởng")
                    .missionType(Mission.MissionType.SPECIAL)
                    .targetType(Mission.TargetType.COMPLETE_PROFILE)
                    .targetValue(1)
                    .rewardPoints(100)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(4)
                    .build(),

                // 5. Special Mission: Verify driver license
                Mission.builder()
                    .title("Xác thực bằng lái xe")
                    .description("Xác thực bằng lái xe để trở thành tài xế và nhận điểm thưởng")
                    .missionType(Mission.MissionType.SPECIAL)
                    .targetType(Mission.TargetType.VERIFY_DRIVER_LICENSE)
                    .targetValue(1)
                    .rewardPoints(200)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(5)
                    .build(),

                // 6. Daily Mission: Rate trips
                Mission.builder()
                    .title("Đánh giá chuyến đi")
                    .description("Đánh giá 5 chuyến đi để nhận điểm thưởng")
                    .missionType(Mission.MissionType.DAILY)
                    .targetType(Mission.TargetType.RATE_TRIPS)
                    .targetValue(5)
                    .rewardPoints(75)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(6)
                    .build(),

                // 7. Weekly Mission: Invite friends
                Mission.builder()
                    .title("Mời bạn bè")
                    .description("Mời 3 bạn bè tham gia RideMate để nhận điểm thưởng")
                    .missionType(Mission.MissionType.WEEKLY)
                    .targetType(Mission.TargetType.INVITE_FRIENDS)
                    .targetValue(3)
                    .rewardPoints(300)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(7)
                    .build(),

                // 8. Monthly Mission: Complete 50 trips
                Mission.builder()
                    .title("Hoàn thành 50 chuyến đi trong tháng")
                    .description("Hoàn thành 50 chuyến đi trong tháng để nhận điểm thưởng lớn")
                    .missionType(Mission.MissionType.MONTHLY)
                    .targetType(Mission.TargetType.COMPLETE_TRIPS)
                    .targetValue(50)
                    .rewardPoints(2000)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(8)
                    .build(),

                // 9. Daily Mission: Consecutive login
                Mission.builder()
                    .title("Đăng nhập liên tiếp 7 ngày")
                    .description("Đăng nhập liên tiếp 7 ngày để nhận điểm thưởng")
                    .missionType(Mission.MissionType.DAILY)
                    .targetType(Mission.TargetType.CONSECUTIVE_DAYS)
                    .targetValue(7)
                    .rewardPoints(200)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(9)
                    .build(),

                // 10. Event Mission: Use vouchers
                Mission.builder()
                    .title("Sử dụng voucher")
                    .description("Sử dụng 3 voucher để nhận điểm thưởng")
                    .missionType(Mission.MissionType.EVENT)
                    .targetType(Mission.TargetType.USE_VOUCHERS)
                    .targetValue(3)
                    .rewardPoints(150)
                    .startDate(now)
                    .endDate(endDate)
                    .isActive(true)
                    .priority(10)
                    .build()
            );

            missionRepository.saveAll(missions);
            log.info("Successfully seeded {} missions", missions.size());
        };
    }
}


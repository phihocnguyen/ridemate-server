package com.ridemate.ridemate_server.application.service.membership;

import com.ridemate.ridemate_server.domain.entity.Membership;
import com.ridemate.ridemate_server.domain.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

/**
 * Seed data for Membership Packages
 * This will run only in development/test profile
 * To use: add --spring.profiles.active=dev to your application.properties or run command
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class MembershipSeedData {

    private final MembershipRepository membershipRepository;

    @Bean
    CommandLineRunner initMembershipData() {
        return args -> {
            if (membershipRepository.count() > 0) {
                log.info("Membership data already exists, skipping seed data");
                return;
            }

            log.info("Seeding membership packages...");

            List<Membership> memberships = Arrays.asList(
                // 1. RideMate Premium
                Membership.builder()
                    .membershipId("MEM-1001")
                    .name("RideMate Premium")
                    .description("Ưu đãi đặc biệt mọi chuyến xe\nTích điểm nhanh gấp đôi")
                    .price(199000)
                    .duration(30)
                    .maxTripsPerDay(5)
                    .pointMultiplier(2.0)
                    .benefits(Arrays.asList(
                        "Giảm 10% mọi chuyến đi",
                        "Tích điểm x2",
                        "Ưu tiên đặt chỗ",
                        "Hỗ trợ 24/7"
                    ))
                    .status(Membership.MembershipStatus.ACTIVE)
                    .build(),

                // 2. RideMate VIP
                Membership.builder()
                    .membershipId("MEM-1002")
                    .name("RideMate VIP")
                    .description("Trải nghiệm dịch vụ cao cấp\nHỗ trợ ưu tiên 24/7")
                    .price(499000)
                    .duration(30)
                    .maxTripsPerDay(10)
                    .pointMultiplier(3.0)
                    .benefits(Arrays.asList(
                        "Giảm 20% mọi chuyến đi",
                        "Tích điểm x3",
                        "Ưu tiên tối đa",
                        "Hỗ trợ VIP 24/7",
                        "Quà tặng đặc biệt"
                    ))
                    .status(Membership.MembershipStatus.ACTIVE)
                    .build(),

                // 3. RideMate Family
                Membership.builder()
                    .membershipId("MEM-1003")
                    .name("RideMate Family")
                    .description("Chia sẻ cho cả gia đình\nTối đa 5 thành viên")
                    .price(299000)
                    .duration(30)
                    .maxTripsPerDay(8)
                    .pointMultiplier(2.5)
                    .benefits(Arrays.asList(
                        "Chia sẻ cho 5 người",
                        "Giảm 15% mọi chuyến đi",
                        "Tích điểm x2.5",
                        "Ưu tiên đặt chỗ"
                    ))
                    .status(Membership.MembershipStatus.ACTIVE)
                    .build(),

                // 4. RideMate Student
                Membership.builder()
                    .membershipId("MEM-1004")
                    .name("RideMate Student")
                    .description("Gói đặc biệt cho sinh viên\nGiá ưu đãi")
                    .price(99000)
                    .duration(30)
                    .maxTripsPerDay(3)
                    .pointMultiplier(1.5)
                    .benefits(Arrays.asList(
                        "Giảm 15% mọi chuyến đi",
                        "Tích điểm x1.5",
                        "Chỉ dành cho sinh viên"
                    ))
                    .status(Membership.MembershipStatus.PAUSED)
                    .build(),

                // 5. RideMate Basic
                Membership.builder()
                    .membershipId("MEM-1005")
                    .name("RideMate Basic")
                    .description("Gói cơ bản cho người mới bắt đầu\nTrải nghiệm membership với giá thấp")
                    .price(49000)
                    .duration(30)
                    .maxTripsPerDay(2)
                    .pointMultiplier(1.2)
                    .benefits(Arrays.asList(
                        "Giảm 5% mọi chuyến đi",
                        "Tích điểm x1.2",
                        "Ưu tiên nhẹ"
                    ))
                    .status(Membership.MembershipStatus.ACTIVE)
                    .build()
            );

            membershipRepository.saveAll(memberships);
            log.info("Successfully seeded {} membership packages", memberships.size());
        };
    }
}


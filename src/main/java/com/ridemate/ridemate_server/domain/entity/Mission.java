package com.ridemate.ridemate_server.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200, columnDefinition = "VARCHAR(200)")
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType missionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;
    
    @Column(nullable = false)
    private Integer targetValue;
    
    @Column(nullable = false)
    private Integer rewardPoints;
    
    @Column
    private Long rewardVoucherId;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer priority = 0;
    
    @Column(columnDefinition = "VARCHAR(500)")
    private String iconUrl;
    
    @Column(columnDefinition = "VARCHAR(500)")
    private String bannerUrl;
    
    public enum MissionType {
        DAILY,      // Nhiệm vụ hàng ngày
        WEEKLY,     // Nhiệm vụ hàng tuần
        MONTHLY,    // Nhiệm vụ hàng tháng
        SPECIAL,    // Nhiệm vụ đặc biệt
        EVENT       // Nhiệm vụ sự kiện
    }
    
    public enum TargetType {
        COMPLETE_TRIPS,         // Hoàn thành X chuyến đi
        EARN_POINTS,            // Kiếm X điểm
        SHARE_TRIPS,            // Chia sẻ X chuyến đi
        INVITE_FRIENDS,         // Mời X bạn bè
        RATE_TRIPS,             // Đánh giá X chuyến đi
        USE_VOUCHERS,           // Sử dụng X voucher
        COMPLETE_PROFILE,       // Hoàn thiện hồ sơ
        VERIFY_DRIVER_LICENSE,  // Xác thực bằng lái
        CONSECUTIVE_DAYS        // Đăng nhập X ngày liên tiếp
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
    
    public boolean isAvailable() {
        LocalDateTime now = LocalDateTime.now();
        return isActive && now.isAfter(startDate) && now.isBefore(endDate);
    }
}

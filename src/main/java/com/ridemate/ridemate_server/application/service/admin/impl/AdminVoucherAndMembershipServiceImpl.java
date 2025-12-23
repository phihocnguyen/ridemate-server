package com.ridemate.ridemate_server.application.service.admin.impl;

import com.ridemate.ridemate_server.application.dto.admin.VoucherStatsResponse;
import com.ridemate.ridemate_server.application.dto.admin.MembershipStatsResponse;
import com.ridemate.ridemate_server.application.service.admin.AdminVoucherAndMembershipService;
import com.ridemate.ridemate_server.domain.entity.UserVoucher;
import com.ridemate.ridemate_server.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVoucherAndMembershipServiceImpl implements AdminVoucherAndMembershipService {

    private final UserVoucherRepository userVoucherRepository;

    @Override
    public List<VoucherStatsResponse> getVoucherStatistics() {
        List<UserVoucher> allUserVouchers = userVoucherRepository.findAll();
        
        Map<String, Long> voucherUsage = allUserVouchers.stream()
                .collect(Collectors.groupingBy(
                        uv -> uv.getVoucher().getVoucherCode(),
                        Collectors.counting()
                ));
        
        long totalUsage = allUserVouchers.size();
        
        return voucherUsage.entrySet().stream()
                .map(entry -> {
                    String voucherCode = entry.getKey();
                    Long count = entry.getValue();
                    double percentage = totalUsage > 0 ? (count * 100.0) / totalUsage : 0;
                    
                    return VoucherStatsResponse.builder()
                            .voucherName(voucherCode)
                            .usageCount(count)
                            .percentage(percentage)
                            .color(getColorByVoucher(voucherCode))
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipStatsResponse> getMembershipStatistics() {
        // Placeholder - cần integrate với membership system nếu có
        // Hiện tại return empty list
        return List.of();
    }

    private String getColorByVoucher(String voucherName) {
        return switch (voucherName.toLowerCase()) {
            case "click & k" -> "#FF6B6B";
            case "highlands" -> "#4ECDC4";
            case "7-dream" -> "#95E1D3";
            case "familymart" -> "#FFB347";
            default -> "#999999";
        };
    }
}

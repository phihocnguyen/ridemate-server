package com.ridemate.ridemate_server.application.service.admin;

import com.ridemate.ridemate_server.application.dto.admin.VoucherStatsResponse;
import com.ridemate.ridemate_server.application.dto.admin.MembershipStatsResponse;

import java.util.List;

public interface AdminVoucherAndMembershipService {
    
    List<VoucherStatsResponse> getVoucherStatistics();
    
    List<MembershipStatsResponse> getMembershipStatistics();
}

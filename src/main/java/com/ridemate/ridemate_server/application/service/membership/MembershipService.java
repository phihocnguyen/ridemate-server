package com.ridemate.ridemate_server.application.service.membership;

import com.ridemate.ridemate_server.application.dto.membership.CreateMembershipRequest;
import com.ridemate.ridemate_server.application.dto.membership.MembershipResponse;
import com.ridemate.ridemate_server.application.dto.membership.UpdateMembershipRequest;

import java.util.List;

public interface MembershipService {
    
    List<MembershipResponse> getAllMemberships();
    
    List<MembershipResponse> getActiveMemberships();
    
    MembershipResponse getMembershipById(Long id);
    
    MembershipResponse getMembershipByMembershipId(String membershipId);
    
    MembershipResponse createMembership(CreateMembershipRequest request);
    
    MembershipResponse updateMembership(Long id, UpdateMembershipRequest request);
    
    void deleteMembership(Long id);
    
    MembershipResponse toggleStatus(Long id);
    
    Long getSubscriberCount(Long membershipId);
}


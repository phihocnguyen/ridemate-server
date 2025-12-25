package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    
    Optional<Membership> findByMembershipId(String membershipId);
    
    List<Membership> findByStatus(Membership.MembershipStatus status);
    
    List<Membership> findByStatusNot(Membership.MembershipStatus status);
    
    boolean existsByMembershipId(String membershipId);
}


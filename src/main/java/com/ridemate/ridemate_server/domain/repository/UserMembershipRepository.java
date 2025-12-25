package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    List<UserMembership> findByUserId(Long userId);
    List<UserMembership> findByUserIdAndStatus(Long userId, UserMembership.MembershipStatus status);
    Optional<UserMembership> findByUserIdAndStatusAndEndDateAfter(Long userId, UserMembership.MembershipStatus status, java.time.LocalDateTime date);
    Optional<UserMembership> findByPaymentId(Long paymentId);
}


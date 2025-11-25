package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
    List<UserVoucher> findByUserIdAndStatus(Long userId, UserVoucher.UserVoucherStatus status);
}

package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.DailySpin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailySpinRepository extends JpaRepository<DailySpin, Long> {
    Optional<DailySpin> findByUserIdAndSpinDate(Long userId, LocalDate spinDate);
    boolean existsByUserIdAndSpinDate(Long userId, LocalDate spinDate);
}


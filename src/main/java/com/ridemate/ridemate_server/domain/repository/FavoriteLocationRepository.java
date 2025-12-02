package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.FavoriteLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoriteLocationRepository extends JpaRepository<FavoriteLocation, Long> {
    List<FavoriteLocation> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
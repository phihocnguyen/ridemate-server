package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Mission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {
    
    // Find active missions
    Page<Mission> findByIsActiveTrue(Pageable pageable);
    
    // Find by mission type
    Page<Mission> findByMissionType(Mission.MissionType missionType, Pageable pageable);
    
    // Find available missions (active and within date range)
    @Query("SELECT m FROM Mission m WHERE m.isActive = true " +
           "AND m.startDate <= :now AND m.endDate >= :now")
    List<Mission> findAvailableMissions(@Param("now") LocalDateTime now);
    
    @Query("SELECT m FROM Mission m WHERE m.isActive = true " +
           "AND m.startDate <= :now AND m.endDate >= :now")
    Page<Mission> findAvailableMissions(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find by type and active status
    @Query("SELECT m FROM Mission m WHERE m.missionType = :type " +
           "AND m.isActive = true AND m.startDate <= :now AND m.endDate >= :now")
    List<Mission> findAvailableMissionsByType(@Param("type") Mission.MissionType type, 
                                                @Param("now") LocalDateTime now);
    
    // Find expired missions
    @Query("SELECT m FROM Mission m WHERE m.endDate < :now")
    List<Mission> findExpiredMissions(@Param("now") LocalDateTime now);
    
    // Count active missions
    Long countByIsActiveTrue();
    
    // Find missions by target type
    List<Mission> findByTargetType(Mission.TargetType targetType);
}

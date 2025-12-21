package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    List<Match> findByPassengerId(Long passengerId);
    List<Match> findByDriverId(Long driverId);
    List<Match> findByStatus(Match.MatchStatus status);
    Page<Match> findByStatus(Match.MatchStatus status, Pageable pageable);
    long countByStatus(Match.MatchStatus status);
    
    // Native query to update JSONB field with explicit casting
    @Modifying
    @Transactional
    @Query(value = "UPDATE matches SET matched_driver_candidates = CAST(:candidates AS JSONB) WHERE id = :matchId", nativeQuery = true)
    void updateMatchedDriverCandidates(@Param("matchId") Long matchId, @Param("candidates") String candidates);
}

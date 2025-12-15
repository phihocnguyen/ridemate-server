package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {
    List<Match> findByPassengerId(Long passengerId);
    List<Match> findByDriverId(Long driverId);
    List<Match> findByStatus(Match.MatchStatus status);
    Page<Match> findByStatus(Match.MatchStatus status, Pageable pageable);
    long countByStatus(Match.MatchStatus status);
}

package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    List<Report> findByReporterId(Long reporterId);
    List<Report> findByStatus(Report.ReportStatus status);
    
    // For admin management
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    
    Page<Report> findByCategory(Report.ReportCategory category, Pageable pageable);
    
    long countByStatus(Report.ReportStatus status);
    
    long countByCategory(Report.ReportCategory category);
}
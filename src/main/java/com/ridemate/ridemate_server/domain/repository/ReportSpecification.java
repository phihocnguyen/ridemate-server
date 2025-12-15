package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Report;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    public static Specification<Report> searchReports(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by status
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // Filter by category
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            // Search in title or description (case-insensitive)
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), 
                        searchPattern
                );
                Predicate descriptionLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), 
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(titleLike, descriptionLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

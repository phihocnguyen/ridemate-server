package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ReportSpecification {

    public static Specification<Report> getReportsByFilter(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm
    ) {
        return (root, query, cb) -> {
            Specification<Report> spec = Specification.where(null);

            if (status != null) {
                spec = spec.and((r, q, b) -> b.equal(r.get("status"), status));
            }

            if (category != null) {
                spec = spec.and((r, q, b) -> b.equal(r.get("category"), category));
            }

            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                spec = spec.and((r, q, b) -> {
                    Join<Report, User> reporter = r.join("reporter", JoinType.LEFT);
                    Join<Report, User> reported = r.join("reportedUser", JoinType.LEFT);
                    
                    return b.or(
                            b.like(b.lower(r.get("title")), likePattern),
                            b.like(b.lower(r.get("description")), likePattern),
                            b.like(b.lower(reporter.get("fullName")), likePattern),
                            b.like(b.lower(reporter.get("phoneNumber")), likePattern),
                            b.like(b.lower(reported.get("fullName")), likePattern)
                    );
                });
            }

            return spec.toPredicate(root, query, cb);
        };
    }
}
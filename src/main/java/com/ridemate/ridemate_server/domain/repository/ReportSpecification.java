package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Report;
import com.ridemate.ridemate_server.domain.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReportSpecification {

    public static Specification<Report> getReportsByFilter(
            Report.ReportStatus status,
            Report.ReportCategory category,
            String searchTerm
    ) {
        return (root, query, cb) -> {
            // FIX QUAN TRỌNG 1: Thêm distinct để tránh lỗi Count Query trong phân trang
            // Giúp Hibernate tính đúng tổng số bản ghi khi có Join
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Status
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            // 2. Lọc theo Category
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            // 3. Tìm kiếm từ khóa (Search Term)
            if (StringUtils.hasText(searchTerm)) {
                String search = searchTerm.trim().toLowerCase();
                String likePattern = "%" + search + "%";

                // FIX QUAN TRỌNG 2: Tái sử dụng Join nếu đã tồn tại để tránh lỗi duplicate join
                Join<Report, User> reporter = getOrCreateJoin(root, "reporter", JoinType.LEFT);
                Join<Report, User> reported = getOrCreateJoin(root, "reportedUser", JoinType.LEFT);

                Predicate searchPredicate = cb.or(
                        // Tìm trong Title
                        cb.like(cb.lower(root.get("title")), likePattern),
                        // Tìm trong Description
                        cb.like(cb.lower(root.get("description")), likePattern),
                        
                        // Tìm theo tên/sđt người báo cáo (Reporter)
                        cb.like(cb.lower(reporter.get("fullName")), likePattern),
                        cb.like(cb.lower(reporter.get("phoneNumber")), likePattern),

                        // Tìm theo tên người bị báo cáo (Reported User) - Check Null an toàn
                        cb.and(
                                cb.isNotNull(root.get("reportedUser")),
                                cb.like(cb.lower(reported.get("fullName")), likePattern)
                        )
                );
                predicates.add(searchPredicate);
            }

            // Nếu không có điều kiện nào, trả về True (Lấy tất cả)
            if (predicates.isEmpty()) {
                return cb.isTrue(cb.literal(true));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Helper method: Kiểm tra xem Join đã tồn tại chưa, nếu chưa thì tạo mới.
     * Giúp tránh lỗi "duplicate association path" khi Hibernate render SQL.
     */
    @SuppressWarnings("unchecked")
    private static Join<Report, User> getOrCreateJoin(jakarta.persistence.criteria.Root<Report> root, String attributeName, JoinType joinType) {
        return (Join<Report, User>) root.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals(attributeName))
                .findFirst()
                .orElseGet(() -> root.join(attributeName, joinType));
    }
}
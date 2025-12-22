package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> searchUsers(
            User.UserType userType,
            Boolean isActive,
            User.DriverApprovalStatus driverApprovalStatus,
            String searchTerm
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by userType
            if (userType != null) {
                predicates.add(criteriaBuilder.equal(root.get("userType"), userType));
            }

            // Filter by isActive
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive));
            }

            // Filter by driverApprovalStatus
            if (driverApprovalStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("driverApprovalStatus"), driverApprovalStatus));
            }

            // Search in fullName, phoneNumber or email (case-insensitive)
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate fullNameLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("fullName")), 
                        searchPattern
                );
                Predicate phoneNumberLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phoneNumber")), 
                        searchPattern
                );
                Predicate emailLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), 
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(fullNameLike, phoneNumberLike, emailLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

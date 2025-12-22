package com.ridemate.ridemate_server.domain.repository;

import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MatchSpecification {
    
    public static Specification<Match> searchTrips(Match.MatchStatus status, String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by status if provided
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Search by searchTerm if provided
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                
                // Use LEFT JOIN for driver (can be null) and passenger
                Join<Match, User> driverJoin = root.join("driver", JoinType.LEFT);
                Join<Match, User> passengerJoin = root.join("passenger", JoinType.LEFT);
                
                List<Predicate> searchPredicates = new ArrayList<>();
                
                // Search in pickup address
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("pickupAddress")),
                    searchPattern
                ));
                
                // Search in destination address
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("destinationAddress")),
                    searchPattern
                ));
                
                // Search in driver's name (if driver exists)
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(driverJoin.get("fullName")),
                    searchPattern
                ));
                
                // Search in driver's phone number (if driver exists)
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(driverJoin.get("phoneNumber")),
                    searchPattern
                ));
                
                // Search in passenger's name
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(passengerJoin.get("fullName")),
                    searchPattern
                ));
                
                // Search in passenger's phone number
                searchPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(passengerJoin.get("phoneNumber")),
                    searchPattern
                ));
                
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(new Predicate[0])));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

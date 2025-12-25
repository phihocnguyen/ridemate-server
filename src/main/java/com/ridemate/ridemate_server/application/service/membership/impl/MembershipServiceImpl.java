package com.ridemate.ridemate_server.application.service.membership.impl;

import com.ridemate.ridemate_server.application.dto.membership.CreateMembershipRequest;
import com.ridemate.ridemate_server.application.dto.membership.MembershipResponse;
import com.ridemate.ridemate_server.application.dto.membership.UpdateMembershipRequest;
import com.ridemate.ridemate_server.application.service.membership.MembershipService;
import com.ridemate.ridemate_server.domain.entity.Membership;
import com.ridemate.ridemate_server.domain.entity.UserMembership;
import com.ridemate.ridemate_server.domain.repository.MembershipRepository;
import com.ridemate.ridemate_server.domain.repository.UserMembershipRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final UserMembershipRepository userMembershipRepository;

    @Override
    public List<MembershipResponse> getAllMemberships() {
        List<Membership> memberships = membershipRepository
                .findByStatusNot(Membership.MembershipStatus.DELETED);
        return memberships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> getActiveMemberships() {
        List<Membership> memberships = membershipRepository
                .findByStatus(Membership.MembershipStatus.ACTIVE);
        return memberships.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MembershipResponse getMembershipById(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));
        return toResponse(membership);
    }

    @Override
    public MembershipResponse getMembershipByMembershipId(String membershipId) {
        Membership membership = membershipRepository.findByMembershipId(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with membershipId: " + membershipId));
        return toResponse(membership);
    }

    @Override
    @Transactional
    public MembershipResponse createMembership(CreateMembershipRequest request) {
        // Check if membershipId already exists
        if (membershipRepository.existsByMembershipId(request.getMembershipId())) {
            throw new IllegalArgumentException("Membership with ID " + request.getMembershipId() + " already exists");
        }

        Membership membership = Membership.builder()
                .membershipId(request.getMembershipId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .duration(request.getDuration())
                .maxTripsPerDay(request.getMaxTripsPerDay())
                .pointMultiplier(request.getPointMultiplier())
                .benefits(request.getBenefits())
                .status(Membership.MembershipStatus.valueOf(request.getStatus()))
                .build();

        membership = membershipRepository.save(membership);
        log.info("Created membership: {} with ID: {}", membership.getName(), membership.getMembershipId());
        
        return toResponse(membership);
    }

    @Override
    @Transactional
    public MembershipResponse updateMembership(Long id, UpdateMembershipRequest request) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            membership.setName(request.getName());
        }
        if (request.getDescription() != null) {
            membership.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            membership.setPrice(request.getPrice());
        }
        if (request.getDuration() != null) {
            membership.setDuration(request.getDuration());
        }
        if (request.getMaxTripsPerDay() != null) {
            membership.setMaxTripsPerDay(request.getMaxTripsPerDay());
        }
        if (request.getPointMultiplier() != null) {
            membership.setPointMultiplier(request.getPointMultiplier());
        }
        if (request.getBenefits() != null) {
            membership.setBenefits(request.getBenefits());
        }
        if (request.getStatus() != null) {
            membership.setStatus(Membership.MembershipStatus.valueOf(request.getStatus()));
        }

        membership = membershipRepository.save(membership);
        log.info("Updated membership: {} with ID: {}", membership.getName(), membership.getMembershipId());
        
        return toResponse(membership);
    }

    @Override
    @Transactional
    public void deleteMembership(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        // Soft delete
        membership.setStatus(Membership.MembershipStatus.DELETED);
        membershipRepository.save(membership);
        log.info("Deleted membership: {} with ID: {}", membership.getName(), membership.getMembershipId());
    }

    @Override
    @Transactional
    public MembershipResponse toggleStatus(Long id) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + id));

        if (membership.getStatus() == Membership.MembershipStatus.ACTIVE) {
            membership.setStatus(Membership.MembershipStatus.PAUSED);
        } else if (membership.getStatus() == Membership.MembershipStatus.PAUSED) {
            membership.setStatus(Membership.MembershipStatus.ACTIVE);
        } else {
            throw new IllegalArgumentException("Cannot toggle status of deleted membership");
        }

        membership = membershipRepository.save(membership);
        log.info("Toggled membership status: {} to {}", membership.getName(), membership.getStatus());
        
        return toResponse(membership);
    }

    @Override
    public Long getSubscriberCount(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found with id: " + membershipId));

        // Count active user memberships with this membershipId
        List<UserMembership> allUserMemberships = userMembershipRepository.findAll();
        return allUserMemberships.stream()
                .filter(um -> um.getMembershipId() != null && um.getMembershipId().equals(membership.getMembershipId()))
                .filter(UserMembership::isActive)
                .count();
    }

    private MembershipResponse toResponse(Membership membership) {
        Long subscribers = getSubscriberCount(membership.getId());
        
        return MembershipResponse.builder()
                .id(membership.getId())
                .membershipId(membership.getMembershipId())
                .name(membership.getName())
                .description(membership.getDescription())
                .price(membership.getPrice())
                .duration(membership.getDuration())
                .maxTripsPerDay(membership.getMaxTripsPerDay())
                .pointMultiplier(membership.getPointMultiplier())
                .benefits(membership.getBenefits())
                .status(membership.getStatus().name())
                .subscribers(subscribers)
                .createdAt(membership.getCreatedAt())
                .updatedAt(membership.getUpdatedAt())
                .build();
    }
}


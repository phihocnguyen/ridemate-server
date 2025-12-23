package com.ridemate.ridemate_server.application.service.notification.impl;

import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.application.dto.notification.NotificationResponse;
import com.ridemate.ridemate_server.application.mapper.NotificationMapper;
import com.ridemate.ridemate_server.application.service.notification.NotificationService;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.Notification;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.NotificationRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.ridemate.ridemate_server.application.service.driver.SupabaseRealtimeService;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private SupabaseRealtimeService supabaseRealtimeService;

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void sendNotification(User user, String title, String body, String type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .type(Notification.NotificationType.valueOf(type))
                .referenceId(referenceId)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
    
    @Override
    public void notifyDriversOfNewMatch(Match match, List<DriverCandidate> candidates) {
        log.info("Sending match notifications to {} driver candidates via Supabase", candidates.size());
        
        // Push to Supabase matches table
        try {
            Map<String, Object> matchData = new HashMap<>();
            matchData.put("id", match.getId());
            matchData.put("passenger_id", match.getPassenger().getId());
            // matchData.put("passenger_name", match.getPassenger().getFullName()); // Column not in Supabase
            // matchData.put("passenger_phone", match.getPassenger().getPhoneNumber()); // Column not in Supabase
            // matchData.put("passenger_avatar", match.getPassenger().getProfilePictureUrl()); // Column not in Supabase
            // matchData.put("pickup_address", match.getPickupAddress()); // Column not in Supabase
            // matchData.put("destination_address", match.getDestinationAddress()); // Column not in Supabase
            matchData.put("pickup_latitude", match.getPickupLatitude());
            matchData.put("pickup_longitude", match.getPickupLongitude());
            matchData.put("destination_latitude", match.getDestinationLatitude());
            matchData.put("destination_longitude", match.getDestinationLongitude());
            // matchData.put("coin", match.getCoin()); // Column not in Supabase
            matchData.put("status", match.getStatus().name());
            matchData.put("created_at", match.getCreatedAt().toString());
            
            // Add driver candidates
            List<Map<String, Object>> candidatesData = candidates.stream()
                .map(c -> {
                    Map<String, Object> cd = new HashMap<>();
                    cd.put("driver_id", c.getDriverId());
                    cd.put("distance", c.getDistanceToPickup());
                    cd.put("eta", c.getEstimatedArrivalTime());
                    return cd;
                })
                .collect(Collectors.toList());
            
            // Serialize to JSON string for Supabase TEXT/JSON column
            try {
                String candidatesJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(candidatesData);
                matchData.put("matched_driver_candidates", candidatesJson);
            } catch (Exception e) {
                log.error("Error serializing driver candidates", e);
                matchData.put("matched_driver_candidates", "[]");
            }
            
            supabaseRealtimeService.publishMatch(matchData);
            
        } catch (Exception e) {
            log.error("Failed to prepare match data for Supabase", e);
        }
        
        // Also save to local database for history/fallback
        for (DriverCandidate candidate : candidates) {
            try {
                User driver = new User();
                driver.setId(candidate.getDriverId());
                
                String title = "🚗 New Ride Request!";
                String body = String.format("From %s to %s. Distance: %.2fkm, Coin: %d", 
                        match.getPickupAddress(),
                        match.getDestinationAddress(),
                        candidate.getDistanceToPickup(),
                        match.getCoin());
                
                // TODO: Revert to "MATCH_REQUEST" once database constraint is updated
                sendNotification(driver, title, body, "SYSTEM", match.getId());
            } catch (Exception e) {
                log.error("Failed to save notification for driver {}: {}", candidate.getDriverId(), e.getMessage());
            }
        }
    }
}
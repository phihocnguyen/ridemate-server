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

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private SupabaseNotificationService supabaseNotificationService;

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
        
        // Push to Supabase Realtime Database for instant notifications
        supabaseNotificationService.notifyDriversOfNewMatch(match, candidates);
        
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
                
                sendNotification(driver, title, body, "MATCH_REQUEST", match.getId());
            } catch (Exception e) {
                log.error("Failed to save notification for driver {}: {}", candidate.getDriverId(), e.getMessage());
            }
        }
    }
}
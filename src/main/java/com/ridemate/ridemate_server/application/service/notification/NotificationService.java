package com.ridemate.ridemate_server.application.service.notification;

import com.ridemate.ridemate_server.application.dto.notification.NotificationResponse;
import com.ridemate.ridemate_server.domain.entity.User;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);   
    void sendNotification(User user, String title, String body, String type, Long referenceId);
}
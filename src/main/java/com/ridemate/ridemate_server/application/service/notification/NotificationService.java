package com.ridemate.ridemate_server.application.service.notification;

import com.ridemate.ridemate_server.application.dto.match.DriverCandidate;
import com.ridemate.ridemate_server.application.dto.notification.NotificationResponse;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);   
    void sendNotification(User user, String title, String body, String type, Long referenceId);
    
    // ✅ NEW: Push notification to Supabase for realtime updates
    /**
     * Send match notification to top drivers via Supabase Realtime
     * @param match The match object
     * @param candidates List of driver candidates sorted by match score
     */
    void notifyDriversOfNewMatch(Match match, List<DriverCandidate> candidates);
}
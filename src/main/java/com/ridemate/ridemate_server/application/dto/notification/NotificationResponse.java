package com.ridemate.ridemate_server.application.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor; 
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; 
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
@Schema(description = "Notification details")
public class NotificationResponse {
    private Long id;
    private String title;
    private String body;
    private String type;
    private Long referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
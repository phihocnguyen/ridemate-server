package com.ridemate.ridemate_server.application.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String content;
    private String type; // TEXT, IMAGE, SYSTEM
    private LocalDateTime createdAt;
}

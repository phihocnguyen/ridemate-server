package com.ridemate.ridemate_server.application.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Message response")
public class MessageResponse {

    @Schema(description = "Message ID", example = "1")
    private Long id;

    @Schema(description = "Session ID", example = "1")
    private Long sessionId;

    @Schema(description = "Sender ID", example = "1")
    private Long senderId;

    @Schema(description = "Sender full name", example = "John Doe")
    private String senderName;

    @Schema(description = "Message content", example = "I will be there in 5 minutes")
    private String messageContent;

    @Schema(description = "Timestamp when message was sent")
    private LocalDateTime timestamp;

    @Schema(description = "Message type", example = "TEXT", allowableValues = {"TEXT", "IMAGE"})
    private String messageType;

    @Schema(description = "Is message read", example = "false")
    private Boolean isRead;

    @Schema(description = "Message creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Message last update timestamp")
    private LocalDateTime updatedAt;
}

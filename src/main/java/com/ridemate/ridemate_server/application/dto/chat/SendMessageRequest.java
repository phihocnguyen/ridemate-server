package com.ridemate.ridemate_server.application.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a chat message")
public class SendMessageRequest {

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @Schema(description = "Message type", example = "TEXT", allowableValues = {"TEXT", "IMAGE"})
    @Builder.Default
    private String type = "TEXT";
}
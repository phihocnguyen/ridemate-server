package com.ridemate.ridemate_server.application.dto.message;

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
@Schema(description = "Request to create a new message")
public class CreateMessageRequest {

    @NotNull(message = "Session ID is required")
    @Schema(description = "Session ID", example = "1")
    private Long sessionId;

    @NotBlank(message = "Message content is required")
    @Schema(description = "Message content", example = "I will be there in 5 minutes")
    private String messageContent;

    @NotNull(message = "Message type is required")
    @Schema(description = "Message type (TEXT or IMAGE)", example = "TEXT", allowableValues = {"TEXT", "IMAGE"})
    private String messageType;
}

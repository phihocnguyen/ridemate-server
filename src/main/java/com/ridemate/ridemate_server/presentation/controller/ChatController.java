package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.chat.MessageResponse;
import com.ridemate.ridemate_server.application.dto.chat.SendMessageRequest;
import com.ridemate.ridemate_server.application.service.chat.ChatService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "Messaging endpoints")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "Send a message", description = "Send a text or image message to a session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal Long userId) {
        
        MessageResponse response = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/history/{sessionId}")
    @Operation(summary = "Get chat history", description = "Get all messages in a session")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getChatHistory(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal Long userId) {
        
        List<MessageResponse> response = chatService.getSessionMessages(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", response));
    }
}
package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.message.CreateMessageRequest;
import com.ridemate.ridemate_server.application.dto.message.MessageResponse;
import com.ridemate.ridemate_server.application.service.message.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Message management API for users in sessions")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new message", description = "Create a new message in a session between users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session or user not found")
    })
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        MessageResponse message = messageService.createMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @GetMapping("/session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all messages in a session", description = "Retrieve all messages for a specific session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<List<MessageResponse>> getMessagesBySession(
            @Parameter(description = "Session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        List<MessageResponse> messages = messageService.getMessagesBySession(sessionId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/session/{sessionId}/paginated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get paginated messages in a session", description = "Retrieve messages for a specific session with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<Page<MessageResponse>> getMessagesBySessionPaginated(
            @Parameter(description = "Session ID", required = true, example = "1")
            @PathVariable Long sessionId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "timestamp"));
        Page<MessageResponse> messages = messageService.getMessagesBySessionPaginated(sessionId, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get message by ID", description = "Retrieve a specific message by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<MessageResponse> getMessageById(
            @Parameter(description = "Message ID", required = true, example = "1")
            @PathVariable Long messageId) {
        MessageResponse message = messageService.getMessageById(messageId);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message marked as read",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<MessageResponse> markMessageAsRead(
            @Parameter(description = "Message ID", required = true, example = "1")
            @PathVariable Long messageId) {
        MessageResponse message = messageService.markMessageAsRead(messageId);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/session/{sessionId}/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark all messages in session as read", description = "Mark all unread messages in a session as read for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All messages marked as read"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session or user not found")
    })
    public ResponseEntity<Map<String, Object>> markAllMessagesAsRead(
            @Parameter(description = "Session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        messageService.markAllMessagesAsRead(sessionId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "All messages marked as read");
        response.put("sessionId", sessionId);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{sessionId}/unread")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread messages", description = "Retrieve all unread messages in a session for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread messages retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<List<MessageResponse>> getUnreadMessages(
            @Parameter(description = "Session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        List<MessageResponse> unreadMessages = messageService.getUnreadMessages(sessionId, userId);
        return ResponseEntity.ok(unreadMessages);
    }

    @GetMapping("/session/{sessionId}/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get unread message count", description = "Get the count of unread messages in a session for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread message count retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<Map<String, Object>> getUnreadMessageCount(
            @Parameter(description = "Session ID", required = true, example = "1")
            @PathVariable Long sessionId) {
        Long userId = getCurrentUserId();
        long unreadCount = messageService.getUnreadMessageCount(sessionId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("userId", userId);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return (Long) authentication.getPrincipal();
    }
}

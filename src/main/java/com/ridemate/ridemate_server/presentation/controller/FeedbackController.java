package com.ridemate.ridemate_server.presentation.controller;

import com.ridemate.ridemate_server.application.dto.feedback.SubmitFeedbackRequest;
import com.ridemate.ridemate_server.application.service.feedback.FeedbackService;
import com.ridemate.ridemate_server.presentation.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
@Tag(name = "Feedback", description = "Ride rating and review endpoints")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Submit feedback", description = "Rate and review a completed ride")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> submitFeedback(
            @Valid @RequestBody SubmitFeedbackRequest request,
            @AuthenticationPrincipal Long userId) {
        
        feedbackService.submitFeedback(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted successfully", null));
    }
}
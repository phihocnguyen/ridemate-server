package com.ridemate.ridemate_server.application.service.feedback;

import com.ridemate.ridemate_server.application.dto.feedback.SubmitFeedbackRequest;

public interface FeedbackService {
    void submitFeedback(Long reviewerId, SubmitFeedbackRequest request);
}
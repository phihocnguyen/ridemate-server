package com.ridemate.ridemate_server.application.service.feedback.impl;

import com.ridemate.ridemate_server.application.dto.feedback.SubmitFeedbackRequest;
import com.ridemate.ridemate_server.application.service.feedback.FeedbackService;
import com.ridemate.ridemate_server.domain.entity.Feedback;
import com.ridemate.ridemate_server.domain.entity.Match;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.FeedbackRepository;
import com.ridemate.ridemate_server.domain.repository.MatchRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void submitFeedback(Long reviewerId, SubmitFeedbackRequest request) {
        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        if (match.getStatus() != Match.MatchStatus.COMPLETED) {
            throw new IllegalArgumentException("Ride must be completed before feedback");
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        
        User reviewed;
        if (match.getPassenger().getId().equals(reviewerId)) {
            if (match.getDriver() == null) throw new IllegalArgumentException("Driver not found for this match");
            reviewed = match.getDriver(); // Khách đánh giá Tài xế
        } else if (match.getDriver() != null && match.getDriver().getId().equals(reviewerId)) {
            reviewed = match.getPassenger(); // Tài xế đánh giá Khách
        } else {
            throw new IllegalArgumentException("You are not a participant of this match");
        }

        if (feedbackRepository.findByMatchIdAndReviewerId(match.getId(), reviewerId).isPresent()) {
            throw new IllegalArgumentException("You have already submitted feedback for this ride");
        }

        // 4. Lưu Feedback
        Feedback feedback = Feedback.builder()
                .match(match)
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        
        feedbackRepository.save(feedback);

        updateUserAverageRating(reviewed);
    }

    private void updateUserAverageRating(User user) {
        List<Feedback> feedbacks = feedbackRepository.findByReviewedId(user.getId());
        if (feedbacks.isEmpty()) return;

        double sum = 0;
        for (Feedback f : feedbacks) {
            sum += f.getRating();
        }
        float average = (float) (sum / feedbacks.size());
        
        user.setRating(average);
        userRepository.save(user);
    }
}

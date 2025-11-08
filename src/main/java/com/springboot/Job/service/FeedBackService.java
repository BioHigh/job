package com.springboot.Job.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.Job.model.FeedBackBean;
import com.springboot.Job.repository.FeedBackRepository;

@Service
public class FeedBackService {

    @Autowired
    private FeedBackRepository feedbackRepository;

    // For users to submit feedback
    public int submitUserFeedback(String message, Integer userId) {
        FeedBackBean feedback = new FeedBackBean();
        feedback.setFbmessage(message);
        feedback.setUser_id(userId);
        feedback.setOwner_id(null);
        return feedbackRepository.saveFeedback(feedback);
    }

    // For owners to submit feedback
    public int submitOwnerFeedback(String message, Integer ownerId) {
        FeedBackBean feedback = new FeedBackBean();
        feedback.setFbmessage(message);
        feedback.setOwner_id(ownerId);
        feedback.setUser_id(null);
        return feedbackRepository.saveFeedback(feedback);
    }

    // Get all feedbacks for admin to view (without pagination)
    public List<FeedBackBean> getAllFeedbacks() {
        return feedbackRepository.getAllFeedbacks();
    }
    
    // Get feedback with pagination
    public List<FeedBackBean> getFeedbacksWithPagination(int offset, int size) {
        return feedbackRepository.findWithPagination(offset, size);
    }
    
    // Count total feedback
    public int getTotalFeedbackCount() {
        return feedbackRepository.countAllFeedbacks();
    }
}
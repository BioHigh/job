package com.springboot.Job.service;

import com.springboot.Job.repository.JobApplicationRepository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    public int getUserApplicationCount(int userId, int jobId) {
        return jobApplicationRepository.countByUserIdAndJobId(userId, jobId);
    }
    
    public boolean canUserApply(int userId, int jobId) {
        int currentCount = getUserApplicationCount(userId, jobId);
        return currentCount < 2; // Allow only 2 applications
    }
    
    public Map<String, Object> getApplicationLimits(int userId, int jobId) {
        Map<String, Object> limits = new HashMap<>();
        int currentCount = getUserApplicationCount(userId, jobId);
        int maxFreeApplications = 2;
        
        limits.put("currentCount", currentCount);
        limits.put("maxFreeApplications", maxFreeApplications);
        limits.put("canApply", currentCount < maxFreeApplications);
        limits.put("remainingFree", Math.max(0, maxFreeApplications - currentCount));
        
        return limits;
    }
}
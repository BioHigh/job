package com.springboot.Job.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.repository.JobPostRepository;

@Service
public class JobPostService {

    @Autowired
    private JobPostRepository jobPostRepository;

    public boolean createJobPost(JobPostBean jobPost) {
        return jobPostRepository.createJobPost(jobPost);
    }
    
    public List<JobPostBean> findRecentJobsByOwner(Integer ownerId, int limit) {
        return jobPostRepository.findRecentJobsByOwner(ownerId, limit);
    }

    public long countActiveJobsByOwner(Integer ownerId) {
        return jobPostRepository.countActiveJobsByOwner(ownerId);
    }

    public long countTotalApplicationsByOwner(Integer ownerId) {
        return jobPostRepository.countTotalApplicationsByOwner(ownerId);
    }

    public long countTotalInterviewsByOwner(Integer ownerId) {
        return jobPostRepository.countTotalInterviewsByOwner(ownerId);
    }
}
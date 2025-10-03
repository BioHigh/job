package com.springboot.Job.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.Job.model.JobPost;
import com.springboot.Job.repository.JobPostRepository;

@Service
public class JobPostService {

    @Autowired
    private JobPostRepository jobPostRepository;

    public boolean createJobPost(JobPost jobPost) {
        return jobPostRepository.createJobPost(jobPost);
    }
}
package com.springboot.Job.service;

import java.util.List;
import java.util.Optional;

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
    
    public List<JobPostBean> findAllJobsByOwner(Integer ownerId) {
        return jobPostRepository.findAllJobsByOwner(ownerId);
    }

    public Optional<JobPostBean> findJobByIdAndOwnerId(Integer jobId, Integer ownerId) {
        return jobPostRepository.findByIdAndOwnerId(jobId, ownerId);
    }

    public boolean updateJobPost(JobPostBean jobPost) {
        return jobPostRepository.updateJobPost(jobPost);
    }

    public boolean deleteJobPost(Integer jobId, Integer ownerId) {
        return jobPostRepository.deleteJobPost(jobId, ownerId);
    }
    
    public List<JobPostBean> findJobsByCompanyId(Integer companyId) {
        return jobPostRepository.findByOwnerId(companyId);
    }
}
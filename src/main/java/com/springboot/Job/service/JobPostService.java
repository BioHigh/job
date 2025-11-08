package com.springboot.Job.service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
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
    
    //8.10.2025
    public Optional<JobPostBean> findJobById(Integer jobId) {
        return jobPostRepository.findJobById(jobId);
    }
    
    //10.10.2025
    public long countAllJobs() {
        return jobPostRepository.countAllJobs();
    }
  
    public List<Map<String, Object>> getActiveJobsWithOwners(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> jobs = jobPostRepository.findActiveJobsWithOwners(pageSize, offset);
        
        for (Map<String, Object> job : jobs) {
            byte[] profilePhoto = (byte[]) job.get("profilePhoto");
            if (profilePhoto != null && profilePhoto.length > 0) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                job.put("profilePhoto", base64Photo);
            }
        }
        
        return jobs;
    }

    public int getTotalPages(int pageSize) {
        long totalJobs = countAllJobs();
        return (int) Math.ceil((double) totalJobs / pageSize);
    }

    // ============ ARCHIVE & RESTORE METHODS ============

    public boolean archiveJob(Integer jobId, String archivedBy, String archiveReason) {
        return jobPostRepository.archiveJob(jobId, archivedBy, archiveReason);
    }

    public boolean archiveJobByOwner(Integer jobId, Integer ownerId, String archiveReason) {
        Optional<JobPostBean> jobOpt = jobPostRepository.findByIdAndOwnerId(jobId, ownerId);
        if (jobOpt.isPresent()) {
            return jobPostRepository.archiveJob(jobId, "OWNER", archiveReason);
        }
        return false;
    }

    // ADD THIS MISSING METHOD
    public boolean restoreJob(Integer jobId) {
        return jobPostRepository.restoreJob(jobId);
    }

    public boolean restoreJobByOwner(Integer jobId, Integer ownerId) {
        Optional<JobPostBean> jobOpt = jobPostRepository.findByIdAndOwnerId(jobId, ownerId);
        if (jobOpt.isPresent() && jobOpt.get().getIsArchived()) {
            return jobPostRepository.restoreJob(jobId);
        }
        return false;
    }

    public boolean deleteJobPermanently(Integer jobId) {
        Optional<JobPostBean> jobOpt = jobPostRepository.findJobById(jobId);
        if (jobOpt.isPresent() && jobOpt.get().getIsArchived()) {
            return jobPostRepository.deleteJobPost(jobId);
        }
        return false;
    }

    public boolean deleteJobPermanentlyByOwner(Integer jobId, Integer ownerId) {
        Optional<JobPostBean> jobOpt = jobPostRepository.findByIdAndOwnerId(jobId, ownerId);
        if (jobOpt.isPresent() && jobOpt.get().getIsArchived()) {
            return jobPostRepository.deleteJobPost(jobId, ownerId);
        }
        return false;
    }

    public List<JobPostBean> findActiveJobsByOwner(Integer ownerId) {
        return jobPostRepository.findActiveJobsByOwner(ownerId);
    }

    public List<JobPostBean> findArchivedJobsByOwner(Integer ownerId) {
        return jobPostRepository.findArchivedJobsByOwner(ownerId);
    }

    public long countArchivedJobsByOwner(Integer ownerId) {
        return jobPostRepository.countArchivedJobsByOwner(ownerId);
    }

    public List<JobPostBean> findAllArchivedJobs() {
        return jobPostRepository.findAllArchivedJobs();
    }

    public long countAllArchivedJobs() {
        return jobPostRepository.countAllArchivedJobs();
    }
    
    
    // add in 15.10.2025
    public List<Map<String, Object>> getJobsByCategoryWithOwners(Integer categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> jobs = jobPostRepository.findJobsByCategoryWithOwners(categoryId, pageSize, offset);
        
        for (Map<String, Object> job : jobs) {
            byte[] profilePhoto = (byte[]) job.get("profilePhoto");
            if (profilePhoto != null && profilePhoto.length > 0) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                job.put("profilePhoto", base64Photo);
            }
        }
        
        return jobs;
    }

    public long countJobsByCategory(Integer categoryId) {
        return jobPostRepository.countJobsByCategory(categoryId);
    }
    
    
    
 // Add these methods to your JobPostService class

    public List<Map<String, Object>> getActiveJobsWithSearch(int page, int pageSize, String search, String location, String jobType) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> jobs = jobPostRepository.findActiveJobsWithSearch(pageSize, offset, search, location, jobType);
        
        for (Map<String, Object> job : jobs) {
            byte[] profilePhoto = (byte[]) job.get("profilePhoto");
            if (profilePhoto != null && profilePhoto.length > 0) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                job.put("profilePhoto", base64Photo);
            }
        }
        
        return jobs;
    }

    public long countActiveJobsWithSearch(String search, String location, String jobType) {
        return jobPostRepository.countActiveJobsWithSearch(search, location, jobType);
    }

    public int getTotalPagesWithSearch(int pageSize, String search, String location, String jobType) {
        long totalJobs = countActiveJobsWithSearch(search, location, jobType);
        return (int) Math.ceil((double) totalJobs / pageSize);
    }
}
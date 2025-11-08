package com.springboot.Job.service;

import com.springboot.Job.model.JobApplicationBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.JobApplicationRepository;
import com.springboot.Job.repository.JobPostRepository;
import com.springboot.Job.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private EmailService emailService;

    // Use the method that includes user details
    public List<JobApplicationBean> getJobApplicationsForOwner(int ownerId) {
        return jobApplicationRepository.findApplicationsWithUserDetails(ownerId);
    }

    // Alternative method that enriches data manually
    public List<JobApplicationBean> getEnrichedApplicationsForOwner(int ownerId) {
        List<JobApplicationBean> applications = jobApplicationRepository.findByOwnerId(ownerId);
        
        return applications.stream()
            .map(app -> {
                try {
                    // Get user details
                    String userName = jobApplicationRepository.getUserName(app.getUserId());
                    if (userName != null) {
                        app.setUserName(userName);
                    }
                    
                    String userEmail = jobApplicationRepository.getUserEmail(app.getUserId());
                    if (userEmail != null) {
                        app.setUserEmail(userEmail);
                    }
                    
                    String userPhone = jobApplicationRepository.getUserPhone(app.getUserId());
                    if (userPhone != null) {
                        app.setUserPhone(userPhone);
                    }
                    
                    // Get job details
                    Optional<JobPostBean> job = jobPostRepository.findJobById(app.getJobId());
                    if (job.isPresent()) {
                        app.setJobTitle(job.get().getJobTitle());
                        app.setCompanyName(job.get().getCompanyName());
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error enriching application " + app.getId() + ": " + e.getMessage());
                }
                return app;
            })
            .collect(Collectors.toList());
    }

    public List<JobApplicationBean> getJobApplicationsByJobId(int jobId, int ownerId) {
        Optional<JobPostBean> job = jobPostRepository.findByIdAndOwnerId(jobId, ownerId);
        if (job.isPresent()) {
            return jobApplicationRepository.findByJobId(jobId);
        }
        return List.of();
    }

    public void sendApplicationNotification(int jobId, int applicationId) {
        try {
            Optional<JobPostBean> job = jobPostRepository.findJobById(jobId);
            Optional<JobApplicationBean> application = jobApplicationRepository.findById(applicationId);
            
            if (job.isPresent() && application.isPresent()) {
                Optional<Owner> owner = ownerRepository.findById(job.get().getOwnerId());
                
                if (owner.isPresent()) {
                    String ownerEmail = owner.get().getGmail();
                    String jobTitle = job.get().getJobTitle();
                    String companyName = owner.get().getCompanyName();
                    
                    String subject = "New Job Application Received - " + jobTitle;
                    String message = String.format(
                        "Dear %s,\n\n" +
                        "You have received a new job application for: %s\n\n" +
                        "Application Details:\n" +
                        "- Job Title: %s\n" +
                        "- Application ID: %d\n" +
                        "- Applied Date: %s\n\n" +
                        "Please login to your dashboard to review the application and download the CV.\n" +
                        "Dashboard Link: http://localhost:8080/owner/messages\n\n" +
                        "Best regards,\n" +
                        "JobJump Team",
                        companyName, jobTitle, jobTitle, applicationId, 
                        application.get().getApplyDate()
                    );
                    
                    emailService.sendApplicationNotification(ownerEmail, subject, message);
                    System.out.println("Application notification email sent to: " + ownerEmail);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to send application notification: " + e.getMessage());
        }
    }

    public boolean updateApplicationStatus(int applicationId, String status, int ownerId) {
        Optional<JobApplicationBean> application = jobApplicationRepository.findById(applicationId);
        
        if (application.isPresent()) {
            Optional<JobPostBean> job = jobPostRepository.findByIdAndOwnerId(
                application.get().getJobId(), ownerId);
            
            if (job.isPresent()) {
                boolean updated = jobApplicationRepository.updateApplicationStatus(applicationId, status);
                
                if (updated && shouldNotifyApplicant(status)) {
                    sendStatusUpdateToApplicant(application.get(), status, job.get());
                }
                return updated;
            }
        }
        return false;
    }

    private boolean shouldNotifyApplicant(String status) {
        return "APPROVED".equals(status) || "REJECTED".equals(status) || "SHORTLISTED".equals(status);
    }

    private void sendStatusUpdateToApplicant(JobApplicationBean application, String status, JobPostBean job) {
        try {
            String userEmail = jobApplicationRepository.getUserEmail(application.getUserId());
            String userName = jobApplicationRepository.getUserName(application.getUserId());
            
            if (userEmail != null) {
                String jobTitle = job.getJobTitle();
                String companyName = job.getCompanyName();
                
                String subject = "Application Status Update - " + jobTitle;
                String message = String.format(
                    "Dear %s,\n\n" +
                    "Your application status for '%s' at %s has been updated.\n\n" +
                    "New Status: %s\n" +
                    "Application ID: %d\n" +
                    "Job Title: %s\n" +
                    "Company: %s\n\n" +
                    "We will contact you if further action is required.\n\n" +
                    "Best regards,\n" +
                    "%s Team",
                    userName != null ? userName : "Applicant", 
                    jobTitle, companyName, status, application.getId(), jobTitle, companyName, companyName
                );
                
                emailService.sendApplicationNotification(userEmail, subject, message);
                System.out.println("Status update email sent to applicant: " + userEmail);
            }
        } catch (Exception e) {
            System.err.println("Failed to send status update to applicant: " + e.getMessage());
        }
    }
}
package com.springboot.Job.controller;

import com.springboot.Job.model.JobApplicationBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.JobApplicationRepository;
import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.MessageService;
import com.springboot.Job.service.OwnerService;
import com.springboot.Job.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/owner")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private JobPostService jobPostService;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @ModelAttribute
    public void updateSessionNotificationCount(HttpSession session) {
        try {
            Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
            if (ownerIdOpt.isPresent()) {
                Integer ownerId = ownerIdOpt.get();
                int unseenCount = jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
                session.setAttribute("newApplicationCount", unseenCount);
                
                System.out.println("🔔 Session Updated - newApplicationCount: " + unseenCount);
            }
        } catch (Exception e) {
            System.err.println("Error updating session count: " + e.getMessage());
            session.setAttribute("newApplicationCount", 0);
        }
    }

    @GetMapping("/messages")
    public String showMessages(HttpSession session, Model model) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return "redirect:/owner/login";
        }

        Integer ownerId = ownerIdOpt.get();
        
        int newApplicationCount = jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
        
        jobApplicationRepository.markAllApplicationsAsSeen(ownerId);
        
        session.setAttribute("newApplicationCount", 0);
        
        List<JobApplicationBean> applications = messageService.getEnrichedApplicationsForOwner(ownerId);
        model.addAttribute("applications", applications);
        model.addAttribute("newApplicationCount", newApplicationCount); 
        
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
        }

        return "owner/messages";
    }
    
    @ModelAttribute
    public void setNewApplicationCount(HttpSession session, Model model) {
        try {
            Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
            if (ownerIdOpt.isPresent()) {
                Integer ownerId = ownerIdOpt.get();
                int count = jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
                session.setAttribute("newApplicationCount", count);
                model.addAttribute("newApplicationCount", count);
                
                System.out.println("DEBUG - Session Count: " + count); // Debug line
            }
        } catch (Exception e) {
            System.err.println("Error setting new application count: " + e.getMessage());
            session.setAttribute("newApplicationCount", 0);
            model.addAttribute("newApplicationCount", 0);
        }
    }

    @GetMapping("/messages/job/{jobId}")
    public String showJobApplications(@PathVariable("jobId") Integer jobId,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return "redirect:/owner/login";
        }

        Integer ownerId = ownerIdOpt.get();
        
        int count = jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
        session.setAttribute("newApplicationCount", count);
        
        List<JobApplicationBean> applications = messageService.getJobApplicationsByJobId(jobId, ownerId);

        Optional<JobPostBean> job = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (job.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found or access denied");
            return "redirect:/owner/messages";
        }

        model.addAttribute("applications", applications);
        model.addAttribute("job", job.get());
        
        ownerService.getOwnerById(ownerId).ifPresent(owner -> {
            model.addAttribute("owner", owner);
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
        });

        return "owner/job-applications";
    }

    @PostMapping("/messages/status/{applicationId}")
    public String updateApplicationStatus(@PathVariable("applicationId") Integer applicationId,
                                        @RequestParam String status,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return "redirect:/owner/login";
        }

        Integer ownerId = ownerIdOpt.get();
        boolean updated = messageService.updateApplicationStatus(applicationId, status, ownerId);
        
        // Update session count
        int newCount = jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
        session.setAttribute("newApplicationCount", newCount);
        
        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Application status updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update application status");
        }

        return "redirect:/owner/messages";
    }

    @GetMapping("/messages/cv/{applicationId}")
    public ResponseEntity<byte[]> downloadCv(@PathVariable("applicationId") Integer applicationId,
                                           HttpSession session) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer ownerId = ownerIdOpt.get();

        byte[] cvFile = jobApplicationRepository.getCvByApplicationId(applicationId);
        
        if (cvFile == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "application_cv.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(cvFile, headers, HttpStatus.OK);
    }
    
    
    @ModelAttribute("newApplicationCount")
    public int getNewApplicationCount(HttpSession session) {
        try {
            Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
            if (ownerIdOpt.isPresent()) {
                Integer ownerId = ownerIdOpt.get();
                return jobApplicationRepository.countUnseenApplicationsByOwnerId(ownerId);
            }
            return 0;
        } catch (Exception e) {
            System.err.println("Error getting new application count: " + e.getMessage());
            return 0;
        }
    }
    
    @PostMapping("/messages/mark-all-read")
    public String markAllAsRead(HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return "redirect:/owner/login";
        }

        Integer ownerId = ownerIdOpt.get();
        boolean success = jobApplicationRepository.markAllApplicationsAsSeen(ownerId);
        
        session.setAttribute("newApplicationCount", 0);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "All applications marked as read");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark applications as read");
        }

        return "redirect:/owner/messages";
    }
    
    @PostMapping("/messages/mark-read/{id}")
    public String markAsRead(@PathVariable("id") Integer applicationId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Optional<Integer> ownerIdOpt = SecurityUtil.getCurrentOwnerId(session);
        if (ownerIdOpt.isEmpty()) {
            return "redirect:/owner/login";
        }

        boolean success = jobApplicationRepository.markApplicationAsSeen(applicationId);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Application marked as read");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to mark application as read");
        }

        return "redirect:/owner/messages";
    }
    
}
package com.springboot.Job.controller;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.CategoryRepository;
import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.OwnerService;
import com.springboot.Job.util.SecurityUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JobPostService jobPostService;
    
    @Autowired
    private CategoryRepository categoryRepo;
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "owner/ownerlogin";
    }

    @PostMapping("/login")
    public String loginOwner(@RequestParam String gmail, 
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        // Check for role conflict first
        if (SecurityUtil.hasRoleConflict(session)) {
            redirectAttributes.addFlashAttribute("error", "Role conflict detected. Please use only one role per browser session.");
            SecurityUtil.clearAllSessions(session);
            return "redirect:/owner/login";
        }
        
        // Clear any existing sessions first
        SecurityUtil.clearAllSessions(session);
        
        Optional<Owner> owner = ownerService.authenticateOwner(gmail, password);
        
        if (owner.isPresent()) {
            session.setAttribute("owner", owner.get());
            session.setAttribute("ownerId", owner.get().getId());
            // Clear any role conflict flag on successful login
            SecurityUtil.setRoleConflict(session, false);
            return "redirect:/owner/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            // Get profile photo separately
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "owner/ownerdashboard";
        } else {
            return "redirect:/owner/login";
        }
    }

    
 // -------------------- ARCHIVE JOB --------------------
    @PostMapping("/job/archive/{id}")
    public String archiveJob(@PathVariable("id") Integer jobId,
                             @RequestParam(required = false) String reason,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<JobPostBean> existingJob = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (existingJob.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to archive it.");
            return "redirect:/owner/jobs";
        }

        boolean isArchived = jobPostService.archiveJobByOwner(jobId, ownerId, reason);
        if (isArchived) {
            redirectAttributes.addFlashAttribute("success", "Job archived successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to archive job. Please try again.");
        }
        return "redirect:/owner/jobs"; // Redirect to main jobs page
    }

    // -------------------- RESTORE JOB --------------------
    @PostMapping("/job/restore/{id}")
    public String restoreJob(@PathVariable("id") Integer jobId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<JobPostBean> existingJob = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (existingJob.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to restore it.");
            return "redirect:/owner/jobs"; // Redirect to main jobs page
        }

        boolean isRestored = jobPostService.restoreJobByOwner(jobId, ownerId);
        if (isRestored) {
            redirectAttributes.addFlashAttribute("success", "Job restored successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to restore job. Please try again.");
        }
        return "redirect:/owner/jobs"; // Redirect to main jobs page
    }
    // -------------------- VIEW ARCHIVED JOBS --------------------
    @GetMapping("/jobs/archived")
    public String viewArchivedJobs(HttpSession session, Model model) {
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            List<JobPostBean> archivedJobs = jobPostService.findArchivedJobsByOwner(ownerId);
            model.addAttribute("jobs", archivedJobs);
            model.addAttribute("owner", owner.get());

            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }

            return "owner/archivedjobs";
        } else {
            return "redirect:/owner/login";
        }
    }
    
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            
            List<JobPostBean> recentJobs = jobPostService.findRecentJobsByOwner(ownerId, 5);
            model.addAttribute("recentJobs", recentJobs);
            
            // Calculate statistics
            long activeJobsCount = jobPostService.countActiveJobsByOwner(ownerId);
           
            
            model.addAttribute("activeJobsCount", activeJobsCount);
            
            
            // Get profile photo separately
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "owner/ownerprofile";
        } else {
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            // Get profile photo for display
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "owner/ownereditprofile";
        } else {
            return "redirect:/owner/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String companyName,
                               @RequestParam String companyPhone,
                               @RequestParam String description,
                               @RequestParam String address,
                               @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Owner owner = new Owner();
        owner.setId(ownerId);
        owner.setCompanyName(companyName);
        owner.setCompanyPhone(companyPhone);
        owner.setDescription(description);
        owner.setAddress(address);

        try {
            boolean isUpdated = ownerService.updateOwnerProfile(owner, profilePhoto);
            
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
                Optional<Owner> updatedOwner = ownerService.getOwnerById(ownerId);
                updatedOwner.ifPresent(o -> session.setAttribute("owner", o));
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update profile");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/owner/profile";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        return "owner/ownerregister";
    }

    @PostMapping("/register")
    public String registerOwner(@RequestParam String companyName,
                              @RequestParam String password,
                              @RequestParam String gmail,
                              @RequestParam(required = false) String companyPhone,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String address,
                              @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                              @RequestParam String confirmPassword,
                              RedirectAttributes redirectAttributes) {
        
        // Validation
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/owner/register";
        }

        if (ownerService.isEmailExists(gmail)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/owner/register";
        }

        if (companyPhone != null && !companyPhone.trim().isEmpty()) {
            if (ownerService.isPhoneExists(companyPhone)) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists");
                return "redirect:/owner/register";
            }
        }

        // Create Owner object
        Owner owner = new Owner();
        owner.setCompanyName(companyName);
        owner.setPassword(password);
        owner.setGmail(gmail);
        owner.setCompanyPhone(companyPhone);
        owner.setDescription(description);
        owner.setAddress(address);

        try {
            boolean isRegistered = ownerService.registerOwner(owner, profilePhoto);
            if (isRegistered) {
                redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
                return "redirect:/owner/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Registration failed");
                return "redirect:/owner/register";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/owner/register";
        }
    }

    @GetMapping("/job/post")
    public String showJobPostingPage(HttpSession session, Model model) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();
        
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            
            model.addAttribute("categories", categoryRepo.findAll());
            
            return "owner/ownerjobposting";
        } else {
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SecurityUtil.clearAllSessions(session);
        session.invalidate();
        return "redirect:/owner/login";
    }
    
    @PostMapping("/job/post")
    public String postJob(@RequestParam String jobTitle,
                         @RequestParam String jobType,
                         @RequestParam(required = false) String department,
                         @RequestParam String location,
                         @RequestParam String jobDescription,
                         @RequestParam(required = false) String companyWebsite,
                         @RequestParam String requiredSkills,
                         @RequestParam(required = false) String experienceLevel,
                         @RequestParam(required = false) String educationLevel,
                         @RequestParam(required = false) Integer salaryMin,
                         @RequestParam(required = false) Integer salaryMax,
                         @RequestParam(required = false) String benefits,
                         @RequestParam(required = false) String applicationDeadline,
                         @RequestParam(required = false) String applicationInstructions,
                         @RequestParam Integer categoryId, 
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        
        System.out.println("=== JOB POSTING STARTED ===");
        
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();
        System.out.println("Owner ID from session: " + ownerId);

        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (!owner.isPresent()) {
            System.out.println("Owner not found - redirecting to login");
            return "redirect:/owner/login";
        }

        System.out.println("Owner found: " + owner.get().getCompanyName());
        System.out.println("Category ID: " + categoryId);
        
        try {
            // Create JobPost object
            JobPostBean jobPost = new JobPostBean();
            jobPost.setJobTitle(jobTitle);
            jobPost.setJobType(jobType);
            jobPost.setDepartment(department);
            jobPost.setLocation(location);
            jobPost.setJobDescription(jobDescription);
            jobPost.setCompanyName(owner.get().getCompanyName());
            jobPost.setCompanyWebsite(companyWebsite);
            jobPost.setCompanyDescription(owner.get().getDescription());
            jobPost.setRequiredSkills(requiredSkills);
            jobPost.setExperienceLevel(experienceLevel);
            jobPost.setEducationLevel(educationLevel);
            jobPost.setSalaryMini(salaryMin);
            jobPost.setSalaryMax(salaryMax);
            jobPost.setBenefits(benefits);
            jobPost.setApplicationEmail(owner.get().getGmail());
            jobPost.setApplicationDeadline(LocalDate.parse(applicationDeadline));
            jobPost.setApplicationInstructions(applicationInstructions);
            jobPost.setOwnerId(ownerId);
            jobPost.setCategoryId(categoryId); // Set the category ID

            System.out.println("Calling jobPostService.createJobPost...");
            boolean isPosted = jobPostService.createJobPost(jobPost);
            System.out.println("Job post result: " + isPosted);
            
            if (isPosted) {
                redirectAttributes.addFlashAttribute("success", "Job posted successfully!");
                System.out.println("Job posted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to post job. Please try again.");
                System.out.println("Failed to post job.");
            }
        } catch (Exception e) {
            System.err.println("Exception while posting job: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while posting the job: " + e.getMessage());
        }
        
        System.out.println("=== JOB POSTING COMPLETED ===");
        return "redirect:/owner/dashboard";
    }
    
    @GetMapping("/jobs")
    public String viewAllJobs(HttpSession session, Model model) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            // Get active and archived jobs separately
            List<JobPostBean> activeJobs = jobPostService.findActiveJobsByOwner(ownerId);
            List<JobPostBean> archivedJobs = jobPostService.findArchivedJobsByOwner(ownerId);
            
            // Add both lists to the model with the correct attribute names
            model.addAttribute("activeJobs", activeJobs);
            model.addAttribute("archivedJobs", archivedJobs);
            model.addAttribute("activeJobCount", activeJobs.size());
            model.addAttribute("archivedJobCount", archivedJobs.size());
            model.addAttribute("owner", owner.get());
            
            // Get profile photo
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            
            return "owner/alljobs";
        } else {
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/job/edit/{id}")
    public String showEditJobPage(@PathVariable("id") Integer jobId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        // Verify that the job belongs to this owner
        Optional<JobPostBean> jobPost = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (jobPost.isPresent()) {
            model.addAttribute("jobPost", jobPost.get());
            model.addAttribute("categories", categoryRepo.findAll());
            
            Optional<Owner> owner = ownerService.getOwnerById(ownerId);
            owner.ifPresent(o -> model.addAttribute("owner", o));
            
            // Get profile photo
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            
            return "owner/editjob";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to edit it.");
            return "redirect:/owner/jobs";
        }
    }

    @PostMapping("/job/update/{id}")
    public String updateJob(@PathVariable("id") Integer jobId,
                           @RequestParam String jobTitle,
                           @RequestParam String jobType,
                           @RequestParam(required = false) String department,
                           @RequestParam String location,
                           @RequestParam String jobDescription,
                           @RequestParam(required = false) String companyWebsite,
                           @RequestParam String requiredSkills,
                           @RequestParam(required = false) String experienceLevel,
                           @RequestParam(required = false) String educationLevel,
                           @RequestParam(required = false) Integer salaryMin,
                           @RequestParam(required = false) Integer salaryMax,
                           @RequestParam(required = false) String benefits,
                           @RequestParam(required = false) String applicationDeadline,
                           @RequestParam(required = false) String applicationInstructions,
                           @RequestParam Integer categoryId,
                           @RequestParam String status,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<JobPostBean> existingJob = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (existingJob.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to edit it.");
            return "redirect:/owner/jobs";
        }

        try {
            // Create updated JobPostBean object
            JobPostBean jobPost = new JobPostBean();
            jobPost.setId(jobId);
            jobPost.setJobTitle(jobTitle);
            jobPost.setJobType(jobType);
            jobPost.setDepartment(department);
            jobPost.setLocation(location);
            jobPost.setJobDescription(jobDescription);
            jobPost.setCompanyWebsite(companyWebsite);
            jobPost.setRequiredSkills(requiredSkills);
            jobPost.setExperienceLevel(experienceLevel);
            jobPost.setEducationLevel(educationLevel);
            jobPost.setSalaryMini(salaryMin);
            jobPost.setSalaryMax(salaryMax);
            jobPost.setBenefits(benefits);
            jobPost.setApplicationDeadline(LocalDate.parse(applicationDeadline));
            jobPost.setApplicationInstructions(applicationInstructions);
            jobPost.setCategoryId(categoryId);
            jobPost.setStatus(status);
            jobPost.setOwnerId(ownerId);

            // Get company info from owner
            Optional<Owner> owner = ownerService.getOwnerById(ownerId);
            if (owner.isPresent()) {
                jobPost.setCompanyName(owner.get().getCompanyName());
                jobPost.setCompanyDescription(owner.get().getDescription());
                jobPost.setApplicationEmail(owner.get().getGmail());
            }

            boolean isUpdated = jobPostService.updateJobPost(jobPost);
            
            if (isUpdated) {
                redirectAttributes.addFlashAttribute("success", "Job updated successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update job. Please try again.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the job: " + e.getMessage());
        }
        
        return "redirect:/owner/jobs";
    }

    @PostMapping("/job/delete/{id}")
    public String deleteJob(@PathVariable("id") Integer jobId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        // Verify ownership before delete
        Optional<JobPostBean> existingJob = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (existingJob.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to delete it.");
            return "redirect:/owner/jobs";
        }

        boolean isDeleted = jobPostService.deleteJobPost(jobId, ownerId);
        
        if (isDeleted) {
            redirectAttributes.addFlashAttribute("success", "Job deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete job. Please try again.");
        }
        
        return "redirect:/owner/jobs";
    }
    
    @GetMapping("/job/view/{id}")
    public String viewJob(@PathVariable("id") Integer jobId,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        // Security handled by interceptor - directly get owner
        Integer ownerId = SecurityUtil.getCurrentOwnerId(session).get();

        Optional<JobPostBean> jobPost = jobPostService.findJobByIdAndOwnerId(jobId, ownerId);
        if (jobPost.isPresent()) {
            model.addAttribute("job", jobPost.get());
            
            Optional<Owner> owner = ownerService.getOwnerById(ownerId);
            owner.ifPresent(o -> model.addAttribute("owner", o));
            
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            
            return "owner/viewjob";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found or you don't have permission to view it.");
            return "redirect:/owner/jobs";
        }
    }
}
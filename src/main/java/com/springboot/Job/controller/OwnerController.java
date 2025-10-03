package com.springboot.Job.controller;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.JobPost;
import com.springboot.Job.model.Owner;
import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.OwnerService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private JobPostService jobPostService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "owner/ownerlogin";
    }

    @PostMapping("/login")
    public String loginOwner(@RequestParam String gmail, 
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Optional<Owner> owner = ownerService.authenticateOwner(gmail, password);
        
        if (owner.isPresent()) {
            session.setAttribute("owner", owner.get());
            session.setAttribute("ownerId", owner.get().getId());
            return "redirect:/owner/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId == null) {
            return "redirect:/owner/login";
        }

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

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId == null) {
            return "redirect:/owner/login";
        }

        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
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
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId == null) {
            return "redirect:/owner/login";
        }

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
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId == null) {
            return "redirect:/owner/login";
        }

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
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId == null) {
            return "redirect:/owner/login";
        }
        
        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (owner.isPresent()) {
            model.addAttribute("owner", owner.get());
            // Get profile photo for display
            byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "owner/ownerjobposting";
        } else {
            return "redirect:/owner/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
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
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        
        System.out.println("=== JOB POSTING STARTED ===");
        
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        System.out.println("Owner ID from session: " + ownerId);
        
        if (ownerId == null) {
            System.out.println("No owner ID in session - redirecting to login");
            return "redirect:/owner/login";
        }

        Optional<Owner> owner = ownerService.getOwnerById(ownerId);
        if (!owner.isPresent()) {
            System.out.println("Owner not found - redirecting to login");
            return "redirect:/owner/login";
        }

        System.out.println("Owner found: " + owner.get().getCompanyName());
        
        // Debug print all parameters
        System.out.println("Job Title: " + jobTitle);
        System.out.println("Job Type: " + jobType);
        System.out.println("Department: " + department);
        System.out.println("Location: " + location);
        System.out.println("Required Skills: " + requiredSkills);
        System.out.println("Experience Level: " + experienceLevel);
        System.out.println("Education Level: " + educationLevel);
        System.out.println("Salary Min: " + salaryMin);
        System.out.println("Salary Max: " + salaryMax);
        System.out.println("Benefits: " + benefits);

        try {
            // Create JobPost object
            JobPost jobPost = new JobPost();
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
            jobPost.setSalaryMin(salaryMin);
            jobPost.setSalaryMax(salaryMax);
            jobPost.setBenefits(benefits);
            jobPost.setApplicationEmail(owner.get().getGmail());
            jobPost.setApplicationDeadline(applicationDeadline);
            jobPost.setApplicationInstructions(applicationInstructions);
            jobPost.setOwnerId(ownerId);

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
}
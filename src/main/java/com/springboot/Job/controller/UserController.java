package com.springboot.Job.controller;

import java.util.ArrayList;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Added this import

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.model.JobApplicationBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.model.UserBean;
import com.springboot.Job.repository.CategoryRepository;
import com.springboot.Job.repository.JobApplicationRepository;
import com.springboot.Job.service.FeedBackService;
import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.MessageService;
import com.springboot.Job.service.OwnerService;
import com.springboot.Job.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private OwnerService ownerService;
    
    @Autowired
    private FeedBackService feedbackService;
    
    @Autowired
    private JobPostService jobPostService;
    
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    
    @Autowired
    private MessageService messageService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "userlogin";
    }
    
    @GetMapping("/index")
    public String showIndex(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            Model model) {

        int pageSize = 4; // same as /home
        List<Map<String, Object>> activeJobs = jobPostService.getActiveJobsWithSearch(page, pageSize, search, location, jobType);
        long totalJobs = jobPostService.countActiveJobsWithSearch(search, location, jobType);
        int totalPages = (int) Math.ceil((double) totalJobs / pageSize);

        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);

        // Keep search/filter values
        model.addAttribute("search", search);
        model.addAttribute("location", location);
        model.addAttribute("jobType", jobType);

        return "index"; // your Thymeleaf template
    }

    
    @GetMapping("/aboutus")
	public String showIndex2() {
	
		return "CuBu/aboutus";
	}
    
    @GetMapping("/contactus")
   	public String showIndex5() {
   	
   		return "CuBu/contactus";
   	}
    @GetMapping("/contact-us")
	public String showIndex3() {
	
		return "CuBu/contactusLogin";
	}
    
    @GetMapping("/about-us")
   	public String showIndex4() {
   	
   		return "CuBu/aboutusLogin";
   	}
       

    @PostMapping("/login")
    public String loginUser(@RequestParam String gmail, 
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Optional<UserBean> user = userService.authenticateUser(gmail, password);
        
        if (user.isPresent()) {
            session.setAttribute("user", user.get());
            session.setAttribute("userId", user.get().getId());
            return "redirect:/user/home"; // Redirect to a home endpoint
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password");
            return "redirect:/user/login";
        }
    }

    //==================12.10.2025 ==========================
    @GetMapping("/home")
    public String showHome(HttpSession session,
                           Model model,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(required = false) String search,
                           @RequestParam(required = false) String location,
                           @RequestParam(required = false) String jobType) {

        // Check user login
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        // Sanitize search filters
        search = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        location = (location != null && !location.trim().isEmpty()) ? location.trim() : null;
        jobType = (jobType != null && !jobType.trim().isEmpty() && !jobType.equalsIgnoreCase("Select Job Type")) ? jobType.trim() : null;

        int pageSize = 4;

        // Fetch jobs and total counts
        List<Map<String, Object>> activeJobs = jobPostService.getActiveJobsWithSearch(page, pageSize, search, location, jobType);
        long totalJobs = jobPostService.countActiveJobsWithSearch(search, location, jobType);
        int totalPages = (int) Math.ceil((double) totalJobs / pageSize);

        // Add attributes to model
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);

        // Keep search/filter values for Thymeleaf
        model.addAttribute("search", search);
        model.addAttribute("location", location);
        model.addAttribute("jobType", jobType);

        // Add user info
        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "home";
        } else {
            return "redirect:/user/login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) String jobType) {

        Integer userId = (Integer) session.getAttribute("userId");
        if(userId == null) return "redirect:/user/login";

        Optional<UserBean> user = userService.getUserById(userId);
        if(user.isPresent()) {
            model.addAttribute("user", user.get());

            int pageSize = 4; // show only 4 jobs per page

            List<Map<String,Object>> activeJobs = jobPostService.getActiveJobsWithSearch(page, pageSize, search, location, jobType);
            long totalJobs = jobPostService.countActiveJobsWithSearch(search, location, jobType);
            int totalPages = jobPostService.getTotalPagesWithSearch(pageSize, search, location, jobType);

            model.addAttribute("activeJobs", activeJobs);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasNext", page < totalPages);
            model.addAttribute("hasPrev", page > 1);

            // Keep search/filter values for Thymeleaf
            model.addAttribute("search", search);
            model.addAttribute("location", location);
            model.addAttribute("jobType", jobType);

            return "userdashboard";
        }
        return "redirect:/user/login";
    }


    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            // Get profile photo separately
            byte[] profilePhoto = userService.getProfilePhoto(userId);
            if (profilePhoto != null) {
                String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                model.addAttribute("profilePhoto", base64Photo);
            }
            return "userprofile";
        } else {
            return "redirect:/user/login";
        }
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        Optional<UserBean> user = userService.getUserById(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "/editprofile";
        } else {
            return "redirect:/user/login";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                               @RequestParam Integer age,
                               @RequestParam String dateOfBirth,
                               @RequestParam String gender,
                               @RequestParam String phone,
                               @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }

        // Create UserBean manually
        UserBean user = new UserBean();
        user.setId(userId);
        user.setName(name);
        user.setAge(age);
        user.setDateOfBirth(dateOfBirth);
        user.setGender(gender);
        user.setPhone(phone);

        boolean isUpdated = userService.updateUserProfile(user, profilePhoto);
        
        if (isUpdated) {
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            Optional<UserBean> updatedUser = userService.getUserById(userId);
            updatedUser.ifPresent(u -> session.setAttribute("user", u));
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile");
        }
        
        return "redirect:/user/profile";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("user", new UserBean());
        return "userregister";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam Integer age,
            @RequestParam String dateOfBirth,
            @RequestParam String password,
            @RequestParam String gmail,
            @RequestParam String gender,
            @RequestParam String phone,
            @RequestParam(required = false) String location,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        // Create UserBean manually
        UserBean user = new UserBean();
        user.setName(name);
        user.setAge(age);
        user.setDateOfBirth(dateOfBirth);
        user.setPassword(password);
        user.setGmail(gmail);
        user.setGender(gender);
        user.setPhone(phone);
        user.setLocation(location);

        // Validation
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/user/register";
        }

        if (userService.isEmailExists(user.getGmail())) {
            redirectAttributes.addFlashAttribute("error", "Email already exists");
            return "redirect:/user/register";
        }

        // Check if phone number already exists
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            if (userService.isPhoneExists(user.getPhone())) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists");
                return "redirect:/user/register";
            }
        }

        boolean isRegistered = userService.registerUser(user, profilePhoto);
        if (isRegistered) {
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/user/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Registration failed");
            return "redirect:/user/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/index";
    }
    
    

    @GetMapping("/by-category")
    public String getJobsByCategory(Model model) {
        List<CategoryBean> categories = categoryRepository.findAll();
        Map<Integer, Integer> jobCounts = categoryRepository.getAllCategoriesWithJobCounts();
        
        model.addAttribute("categories", categories);
        model.addAttribute("jobCounts", jobCounts);
        return "jobs-by-category";
    }
    
    //===============fix 9.10.2025=====================
   
 // Guest view: companies with search + pagination (no login required)
  

    @GetMapping("/company/{id}")
    public String viewCompanyJobs(@PathVariable("id") Integer companyId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Owner> company = ownerService.getOwnerById(companyId);
        if (company.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Company not found");
            return "redirect:/user_companies";
        }
        
        List<JobPostBean> companyJobs = jobPostService.findJobsByCompanyId(companyId);
        
        model.addAttribute("company", company.get());
        model.addAttribute("companyJobs", companyJobs);
        
        byte[] profilePhoto = ownerService.getProfilePhoto(companyId);
        if (profilePhoto != null) {
            String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
            model.addAttribute("companyProfilePhoto", base64Photo);
        }
        
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        if (ownerId != null) {
            Optional<Owner> loggedInOwner = ownerService.getOwnerById(ownerId);
            loggedInOwner.ifPresent(o -> {
                model.addAttribute("owner", o);
                byte[] ownerProfilePhoto = ownerService.getProfilePhoto(ownerId);
                if (ownerProfilePhoto != null) {
                    String base64OwnerPhoto = Base64.getEncoder().encodeToString(ownerProfilePhoto);
                    model.addAttribute("profilePhoto", base64OwnerPhoto);
                }
            });
        }
        
        return "company-jobs";
    }
   

    
    //8.10.2025
    @GetMapping("/job/detail/{id}")
    public String viewJobDetail(@PathVariable("id") Integer jobId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Check if user is logged in (either owner or user)
        Integer ownerId = (Integer) session.getAttribute("ownerId");
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Find the job by ID
        Optional<JobPostBean> jobPost = jobPostService.findJobById(jobId);
        
        if (jobPost.isPresent()) {
            model.addAttribute("job", jobPost.get());
            
            // Get company info
            Optional<Owner> company = ownerService.getOwnerById(jobPost.get().getOwnerId());
            if (company.isPresent()) {
                model.addAttribute("company", company.get());
                
                // Get company profile photo
                byte[] companyProfilePhoto = ownerService.getProfilePhoto(company.get().getId());
                if (companyProfilePhoto != null) {
                    String base64Photo = Base64.getEncoder().encodeToString(companyProfilePhoto);
                    model.addAttribute("companyProfilePhoto", base64Photo);
                }
            }
            
            // Add user type information for template
            if (ownerId != null) {
                model.addAttribute("userType", "owner");
                Optional<Owner> owner = ownerService.getOwnerById(ownerId);
                owner.ifPresent(o -> {
                    model.addAttribute("owner", o);
                    byte[] profilePhoto = ownerService.getProfilePhoto(ownerId);
                    if (profilePhoto != null) {
                        String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                        model.addAttribute("profilePhoto", base64Photo);
                    }
                });
            } else if (userId != null) {
                model.addAttribute("userType", "user");
                Optional<UserBean> user = userService.getUserById(userId);
                user.ifPresent(u -> {
                    model.addAttribute("user", u);
                    byte[] profilePhoto = userService.getProfilePhoto(userId);
                    if (profilePhoto != null) {
                        String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                        model.addAttribute("profilePhoto", base64Photo);
                    }
                });
            } else {
                model.addAttribute("userType", "guest");
            }
            
            return "job-detail";
        } else {
            redirectAttributes.addFlashAttribute("error", "Job not found.");
            return "redirect:/user/user_companies";
        }
    }
    
 // add in 15.10.2025
    @GetMapping("/user-jobs-by-category")
    public String showJobsByCategory(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }
        
        List<CategoryBean> categories = categoryRepository.findAll();
        Map<Integer, Integer> jobCounts = categoryRepository.getAllCategoriesWithJobCounts();
        
        model.addAttribute("categories", categories);
        model.addAttribute("jobCounts", jobCounts != null ? jobCounts : new HashMap<>());
        
        Optional<UserBean> user = userService.getUserById(userId);
        user.ifPresent(u -> model.addAttribute("user", u));
        
        return "user-jobs-by-category";
    }
    
    @GetMapping("/api/jobs/category/{categoryId}")
    @ResponseBody    
    public Map<String, Object> getJobsByCategoryApi(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") int page) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int pageSize = 10;
            List<Map<String, Object>> jobs = jobPostService.getJobsByCategoryWithOwners(categoryId, page, pageSize);
            long totalJobs = jobPostService.countJobsByCategory(categoryId);
            
            response.put("jobs", jobs);
            response.put("totalJobs", totalJobs);
            response.put("success", true);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to load jobs");
            response.put("success", false);
        }
        
        return response;
    }
    
    @GetMapping("/user-jobs-by-category/{categoryId}")
    public String showJobsBySpecificCategory(
            @PathVariable Integer categoryId,
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "1") int page) {
        
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }
        
        try {
            Optional<CategoryBean> category = categoryRepository.findById(categoryId);
            if (category.isPresent()) {
                model.addAttribute("selectedCategory", category.get());
            }
            
            int pageSize = 10;
            List<Map<String, Object>> jobs = jobPostService.getJobsByCategoryWithOwners(categoryId, page, pageSize);
            long totalJobs = jobPostService.countJobsByCategory(categoryId);
            int totalPages = (int) Math.ceil((double) totalJobs / pageSize);
            
            model.addAttribute("jobs", jobs);
            model.addAttribute("totalJobs", totalJobs);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            
            List<CategoryBean> categories = categoryRepository.findAll();
            Map<Integer, Integer> jobCounts = categoryRepository.getAllCategoriesWithJobCounts();
            
            model.addAttribute("categories", categories);
            model.addAttribute("jobCounts", jobCounts != null ? jobCounts : new HashMap<>());
            
            Optional<UserBean> user = userService.getUserById(userId);
            user.ifPresent(u -> model.addAttribute("user", u));
            
            return "user-jobs-by-category";
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/user-jobs-by-category";
        }
    }
    @PostMapping("/feedback/submit")
    public String submitUserFeedback(@RequestParam String fbmessage,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    @RequestHeader(value = "referer", required = false) String referer) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login";
        }
        
        try {
            feedbackService.submitUserFeedback(fbmessage, userId);
            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit feedback: " + e.getMessage());
        }
        
        // Redirect back to the same page where the feedback was submitted
        if (referer != null) {
            return "redirect:" + referer;
        } else {
            return "redirect:/user/dashboard"; // fallback
        }
    }

    @PostMapping("/feedback")
    public String handleFeedback(@RequestParam String fbmessage) {
        // Process feedback here
        System.out.println("Feedback received: " + fbmessage);
        return "redirect:/user/login"; // Redirect to login after feedback
    }
    
    
    
  //===========================================================
    // job apply fix in 17.10.2025 =====================================
     @PostMapping("/apply-job")
     public String applyJob(
             @RequestParam("userId") int userId,
             @RequestParam("jobId") int jobId,
             @RequestParam("cvFile") MultipartFile cv, 
             RedirectAttributes ra,
             HttpSession session) {

         System.out.println("=== DEBUG: Starting apply-job method ===");
         System.out.println("User ID: " + userId);
         System.out.println("Job ID: " + jobId);
         System.out.println("CV File: " + (cv != null ? cv.getOriginalFilename() : "NULL"));
         System.out.println("CV Size: " + (cv != null ? cv.getSize() : "0"));
         System.out.println("CV Content Type: " + (cv != null ? cv.getContentType() : "NULL"));

         // 1. Validate session
         UserBean sessionUser = (UserBean) session.getAttribute("user");
         if (sessionUser == null || sessionUser.getId() != userId) {
             System.out.println("❌ Session validation failed");
             ra.addFlashAttribute("errorMsg", "⚠️ Please log in to apply for jobs.");
             return "redirect:/user/login";
         }
         System.out.println("✅ Session validation passed");

         // 2. Check if CV file is missing or empty
         if (cv == null || cv.isEmpty()) {
             System.out.println("❌ CV file is empty");
             ra.addFlashAttribute("errorMsg", "⚠️ CV file is missing. Please upload your document.");
             return "redirect:/user/job/detail/" + jobId; 
         }
         System.out.println("✅ CV file validation passed");

         // 3. Check if the file type is PDF
         if (!"application/pdf".equalsIgnoreCase(cv.getContentType())) {
             System.out.println("❌ File type validation failed: " + cv.getContentType());
             ra.addFlashAttribute("errorMsg", "⚠️ Only PDF files are allowed for CV submission.");
             return "redirect:/user/job/detail/" + jobId;
         }
         System.out.println("✅ File type validation passed");

         try {
             // 4. Convert MultipartFile to byte[]
             byte[] cvBytes = cv.getBytes();
             System.out.println("✅ CV converted to bytes: " + cvBytes.length + " bytes");
             
             // 5. Constructing the Application Object
             JobApplicationBean app = new JobApplicationBean(); 
             app.setUserId(userId);
             app.setJobId(jobId);
             app.setCvFile(cvBytes);
             System.out.println("✅ Application object created");
             
             // 6. Save application
             System.out.println("📝 Calling saveJobApplication...");
             int result = jobApplicationRepository.saveJobApplication(app); 
             System.out.println("📊 saveJobApplication result: " + result);
             
             // 7. Check for success/failure
             if (result > 0) {
                 System.out.println("✅ Application saved successfully to database");
                 ra.addFlashAttribute("successMsg", "✅ Your job application has been submitted successfully!");
                 
                 // Send email notification to owner
                 try {
                     // Get the most recent application for this job to get the ID
                     List<JobApplicationBean> recentApplications = jobApplicationRepository.findByJobId(jobId);
                     if (!recentApplications.isEmpty()) {
                         int applicationId = recentApplications.get(0).getId();
                         System.out.println("📧 Sending notification for application ID: " + applicationId);
                         messageService.sendApplicationNotification(jobId, applicationId);
                     }
                 } catch (Exception e) {
                     System.err.println("❌ Failed to send notification email: " + e.getMessage());
                     // Don't fail the application if email fails
                 }
                 
             } else {
                 System.out.println("❌ Application failed to save - result was 0");
                 ra.addFlashAttribute("errorMsg", "❌ Failed to submit application. Please try again later.");
             }

         } catch (Exception e) {
             System.err.println("❌ Exception in applyJob method:");
             e.printStackTrace();
             ra.addFlashAttribute("errorMsg", "❌ Error processing CV file: " + e.getMessage());
             return "redirect:/user/job/detail/" + jobId;
         }

         System.out.println("=== DEBUG: apply-job method completed ===");
         // 8. Redirect back to the Job Detail Page
         return "redirect:/user/job/detail/" + jobId;
     }
     
  // Application Form Page
     @GetMapping("/apply-form/{jobId}")
     public String showApplyForm(@PathVariable("jobId") int jobId, 
                               HttpSession session, 
                               Model model,
                               RedirectAttributes ra) {
         
         // Check if user is logged in
         UserBean user = (UserBean) session.getAttribute("user");
         if (user == null) {
             ra.addFlashAttribute("errorMsg", "Please login to apply for jobs");
             return "redirect:/user/login";
         }
         
         // Get job details
         Optional<JobPostBean> job = jobPostService.findJobById(jobId);
         if (job.isEmpty()) {
             ra.addFlashAttribute("errorMsg", "Job not found");
             return "redirect:/user/user_companies";
         }
         
         // Get application count for this user and job
         int applicationCount = jobApplicationRepository.countByUserIdAndJobId(user.getId(), jobId);
         int maxApplications = 2; // Free users get 2 applications
         
         model.addAttribute("job", job.get());
         model.addAttribute("user", user);
         model.addAttribute("applicationCount", applicationCount);
         model.addAttribute("maxApplications", maxApplications);
         
         return "Formjob";
     }
     
  // =========================================================================
     //  Job Application user can view CV 20-10-25(8:00PM)
     // =========================================================================
  // User Application History
     @GetMapping("/resume")
     public String showMyApplications(HttpSession session, 
                                    Model model, 
                                    RedirectAttributes ra,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(required = false) String status) {
         
         UserBean user = (UserBean) session.getAttribute("user");
         if (user == null) {
             ra.addFlashAttribute("errorMsg", "Please login to view your applications");
             return "redirect:/user/login";
         }
         
         int pageSize = 5; // Limit of 5 applications per page
         List<Map<String, Object>> applications;
         long totalCount;
         long filteredCount;
         
         // Get applications based on status filter
         if (status != null && !status.isEmpty() && !status.equals("ALL")) {
             // Use status-filtered methods
             applications = jobApplicationRepository.getUserApplicationsPaginatedByStatus(
                 user.getId(), status, page, pageSize);
             totalCount = jobApplicationRepository.getUserApplicationsCount(user.getId());
             filteredCount = jobApplicationRepository.getUserApplicationsCountByStatus(user.getId(), status);
         } else {
             // Get all applications
             applications = jobApplicationRepository.getUserApplicationsPaginated(
                 user.getId(), page, pageSize);
             totalCount = jobApplicationRepository.getUserApplicationsCount(user.getId());
             filteredCount = totalCount;
             status = "ALL"; // Set default status
         }
         
         int totalPages = (int) Math.ceil((double) filteredCount / pageSize);
         
         // Get statistics for all statuses
         Map<String, Long> stats = jobApplicationRepository.getApplicationStats(user.getId());
         
         model.addAttribute("applications", applications);
         model.addAttribute("user", user);
         model.addAttribute("currentPage", page);
         model.addAttribute("totalPages", totalPages);
         model.addAttribute("totalCount", totalCount);
         model.addAttribute("filteredCount", filteredCount);
         model.addAttribute("selectedStatus", status);
         model.addAttribute("pageSize", pageSize);
         
         // Add individual counts for the filter buttons
         model.addAttribute("pendingCount", stats.getOrDefault("PENDING", 0L));
         model.addAttribute("approvedCount", stats.getOrDefault("APPROVED", 0L));
         model.addAttribute("reviewedCount", stats.getOrDefault("REVIEWED", 0L));
         model.addAttribute("rejectedCount", stats.getOrDefault("REJECTED", 0L));
         
         return "my-applications";
     }
     // Download CV
     @GetMapping("/download-cv/{applicationId}")
     public ResponseEntity<byte[]> downloadCV(@PathVariable("applicationId") int applicationId, 
                                            HttpSession session) {
         UserBean user = (UserBean) session.getAttribute("user");
         if (user == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         }
         
         // Check if the application belongs to the user
         Map<String, Object> application = jobApplicationRepository.getApplicationById(applicationId);
         if (application == null || !application.get("user_id").equals(user.getId())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
         }
         
         byte[] cvData = jobApplicationRepository.getCVData(applicationId);
         if (cvData == null) {
             return ResponseEntity.notFound().build();
         }
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_PDF);
         headers.setContentDisposition(ContentDisposition.builder("attachment")
                 .filename("CV_" + application.get("job_title") + "_" + application.get("company_name") + ".pdf")
                 .build());
         headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
         
         return new ResponseEntity<>(cvData, headers, HttpStatus.OK);
     }

     // View CV in browser
     @GetMapping("/view-cv/{applicationId}")
     public ResponseEntity<byte[]> viewCV(@PathVariable("applicationId") int applicationId, 
                                        HttpSession session) {
         UserBean user = (UserBean) session.getAttribute("user");
         if (user == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         }
         
         // Check if the application belongs to the user
         Map<String, Object> application = jobApplicationRepository.getApplicationById(applicationId);
         if (application == null || !application.get("user_id").equals(user.getId())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
         }
         
         byte[] cvData = jobApplicationRepository.getCVData(applicationId);
         if (cvData == null) {
             return ResponseEntity.notFound().build();
         }
         
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_PDF);
         headers.setContentDisposition(ContentDisposition.builder("inline")
                 .filename("CV_" + application.get("job_title") + "_" + application.get("company_name") + ".pdf")
                 .build());
         
         return new ResponseEntity<>(cvData, headers, HttpStatus.OK);
     }
     
     
     
  // =================== PROFESSIONAL PROFILE METHODS ===================

  // Display profile details page
     @GetMapping("/profile/details")
     public String showProfileDetails(HttpSession session, Model model) {
         Integer userId = (Integer) session.getAttribute("userId");
         if (userId == null) {
             return "redirect:/user/login";
         }

         Optional<UserBean> user = userService.getUserById(userId);
         if (user.isPresent()) {
             model.addAttribute("user", user.get());
             
             // Get profile photo
             byte[] profilePhoto = userService.getProfilePhoto(userId);
             if (profilePhoto != null) {
                 String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                 model.addAttribute("profilePhoto", base64Photo);
             }
             
             // Check if user has resume
             byte[] resume = userService.getResume(userId);
             model.addAttribute("hasResume", resume != null);
             
             return "userprofiledetail";
         } else {
             return "redirect:/user/login";
         }
     }
  // Show edit professional profile form
  @GetMapping("/profile/details/edit")
  public String showEditProfessionalProfile(HttpSession session, Model model) {
      Integer userId = (Integer) session.getAttribute("userId");
      if (userId == null) {
          return "redirect:/user/login";
      }

      Optional<UserBean> user = userService.getUserById(userId);
      if (user.isPresent()) {
          model.addAttribute("user", user.get());
          return "userprofiledetail"; // This will be your Thymeleaf template
      } else {
          return "redirect:/user/login";
      }
  }

  // Update professional profile
  @PostMapping("/profile/details/update")
  public String updateProfessionalProfile(
          @RequestParam(required = false) String profession,
          @RequestParam(required = false) Integer experienceYears,
          @RequestParam(required = false) String education,
          @RequestParam(required = false) String location,
          @RequestParam(required = false) String skills,
          @RequestParam(required = false) MultipartFile resumeFile, // Add this parameter
          HttpSession session,
          RedirectAttributes redirectAttributes) {
      
      Integer userId = (Integer) session.getAttribute("userId");
      if (userId == null) {
          return "redirect:/user/login";
      }

      try {
          // Create UserBean with professional data
          UserBean user = new UserBean();
          user.setId(userId);
          user.setProfession(profession);
          user.setExperienceYears(experienceYears);
          user.setEducation(education);
          user.setLocation(location);
          user.setSkills(skills);

          boolean isUpdated = userService.updateProfessionalProfile(user);
          
          // Handle resume upload separately
          if (resumeFile != null && !resumeFile.isEmpty()) {
              boolean resumeUpdated = userService.updateResume(userId, resumeFile);
              if (resumeUpdated) {
                  redirectAttributes.addFlashAttribute("success", 
                      "Professional profile and resume updated successfully!");
              } else {
                  redirectAttributes.addFlashAttribute("success", 
                      "Professional profile updated but failed to update resume");
              }
          } else {
              redirectAttributes.addFlashAttribute("success", "Professional profile updated successfully!");
          }
          
          // Update session user data
          Optional<UserBean> updatedUser = userService.getUserById(userId);
          updatedUser.ifPresent(u -> session.setAttribute("user", u));
          
      } catch (Exception e) {
          redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
      }
      
      return "redirect:/user/profile/details";
  }
  
  
//Download Resume
@GetMapping("/resume/download")
public ResponseEntity<byte[]> downloadResume(HttpSession session) {
   Integer userId = (Integer) session.getAttribute("userId");
   if (userId == null) {
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
   }
   
   byte[] resumeData = userService.getResume(userId);
   if (resumeData == null) {
       return ResponseEntity.notFound().build();
   }
   
   HttpHeaders headers = new HttpHeaders();
   headers.setContentType(MediaType.APPLICATION_PDF);
   headers.setContentDisposition(ContentDisposition.builder("attachment")
           .filename("resume.pdf")
           .build());
   
   return new ResponseEntity<>(resumeData, headers, HttpStatus.OK);
}

//View Resume in browser
@GetMapping("/resume/view")
public ResponseEntity<byte[]> viewResume(HttpSession session) {
   Integer userId = (Integer) session.getAttribute("userId");
   if (userId == null) {
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
   }
   
   byte[] resumeData = userService.getResume(userId);
   if (resumeData == null) {
       return ResponseEntity.notFound().build();
   }
   
   HttpHeaders headers = new HttpHeaders();
   headers.setContentType(MediaType.APPLICATION_PDF);
   headers.setContentDisposition(ContentDisposition.builder("inline")
           .filename("resume.pdf")
           .build());
   
   return new ResponseEntity<>(resumeData, headers, HttpStatus.OK);
}
     // Additional helper methods if needed
     
     /**
      * Get application by ID (for security validation)
      */
     private boolean isUserAuthorized(int applicationId, int userId) {
         Map<String, Object> application = jobApplicationRepository.getApplicationById(applicationId);
         return application != null && application.get("user_id").equals(userId);
     }
     
   //4-11-25 Pagination update with search
     @GetMapping("/user_companies")
     public String showUserCompanies(
             @RequestParam(defaultValue = "1") int page,
             @RequestParam(required = false) String search,
             HttpSession session, 
             Model model) {
         
         System.out.println("=== User view companies accessed ===");
         System.out.println("Search parameter: " + search);
         
         int pageSize = 6;
         
         // Get companies with pagination and search
         List<Owner> companies;
         int totalPages;
         int totalActiveCompanies;
         int totalFilteredCompanies = 0;
         
         if (search != null && !search.trim().isEmpty()) {
             // Search mode
             companies = ownerService.searchActiveCompaniesWithPagination(search, page, pageSize);
             totalFilteredCompanies = ownerService.countSearchActiveCompanies(search);
             totalPages = (int) Math.ceil((double) totalFilteredCompanies / pageSize);
             totalActiveCompanies = ownerService.getTotalActiveCompanies(); // For total count display
         } else {
             // Normal mode
             companies = ownerService.getActiveCompaniesWithPagination(page, pageSize);
             totalActiveCompanies = ownerService.getTotalActiveCompanies();
             totalPages = ownerService.getTotalPages(pageSize);
             totalFilteredCompanies = totalActiveCompanies;
         }
         
         // Convert to companiesWithPhotos
         List<Map<String, Object>> companiesWithPhotos = new ArrayList<>();
         
         for (Owner company : companies) {
             Map<String, Object> companyMap = new HashMap<>();
             companyMap.put("company", company);
             
             byte[] profilePhoto = ownerService.getProfilePhoto(company.getId());
             if (profilePhoto != null) {
                 String base64Photo = Base64.getEncoder().encodeToString(profilePhoto);
                 companyMap.put("profilePhotoBase64", base64Photo);
             } else {
                 companyMap.put("profilePhotoBase64", null);
             }
             
             companiesWithPhotos.add(companyMap);
         }
         
         model.addAttribute("companiesWithPhotos", companiesWithPhotos);
         model.addAttribute("currentPage", page);
         model.addAttribute("totalPages", totalPages);
         model.addAttribute("totalActiveCompanies", totalActiveCompanies);
         model.addAttribute("totalFilteredCompanies", totalFilteredCompanies);
         model.addAttribute("search", search);
         
         Integer userId = (Integer) session.getAttribute("userId");
         if (userId != null) {
             // User session info if needed
             Optional<UserBean> user = userService.getUserById(userId);
             user.ifPresent(u -> model.addAttribute("user", u));
         }
         
         return "user_companies";
     }
}
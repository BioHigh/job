package com.springboot.Job.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.AdminLoginBean;
import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.model.FeedBackBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.LoginBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.repository.AdminLoginRepository;
import com.springboot.Job.repository.CategoryRepository;
import com.springboot.Job.repository.FeedBackRepository;
import com.springboot.Job.repository.JobPVByAdmRepository;
import com.springboot.Job.repository.JobPostRepository;
import com.springboot.Job.repository.OwnerRepository;
import com.springboot.Job.util.SecurityUtil;
import com.springboot.Job.model.OwnerRequest;
import com.springboot.Job.repository.OwnerRequestRepository;
import com.springboot.Job.service.DashboardService;
import com.springboot.Job.service.EmailService;
import com.springboot.Job.service.FeedBackService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminLoginRepository adminRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    @Autowired
    private JobPVByAdmRepository jobPVByAdmRepo;
    
    @Autowired
    private JobPostRepository jobPostRepository;
   
    @Autowired
    private OwnerRequestRepository ownerRequestRepository;

    
    @Autowired
    private FeedBackService feedbackService;
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OwnerRepository ownerRepository;

    // =================== LOGIN ====================
    @GetMapping("/admin")
    public ModelAndView showadmLogin() {
        return new ModelAndView("admin/AdminLogin", "admlogin", new LoginBean());
    }

    @PostMapping("/admindashboard")
    public String dologin(@ModelAttribute("admlogin") LoginBean obj,
                          HttpSession session,
                          RedirectAttributes ra) {
        
        // Check for role conflict first
        if (SecurityUtil.hasRoleConflict(session)) {
            ra.addFlashAttribute("msg", "Role conflict detected. Please use only one role per browser session.");
            SecurityUtil.clearAllSessions(session);
            return "redirect:/admin";
        }
        
        // Clear any existing sessions first
        SecurityUtil.clearAllSessions(session);
        
        List<AdminLoginBean> adm = adminRepo.loginAdmin(obj);

        if (adm.isEmpty()) {
            ra.addFlashAttribute("msg", "Incorrect name and password!");
            return "redirect:/admin";
        } else {
            AdminLoginBean admin = adm.get(0);
            session.setAttribute("loginadm", admin);
            session.setAttribute("adminName", admin.getName());
            // Clear any role conflict flag on successful login
            SecurityUtil.setRoleConflict(session, false);
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(
        @RequestParam(required = false) String month,
        @RequestParam(required = false) String year,
        HttpSession session, 
        Model model) {
        
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        // Handle both year and month parameters
        String selectedMonth = (month != null && !month.equals("current")) ? month : getCurrentMonthName();
        String selectedYear = (year != null && !year.equals("current")) ? year : String.valueOf(java.time.Year.now().getValue());
        
        // Get dashboard stats for selected year + month
        Map<String, Object> dashboardStats = dashboardService.getDashboardStats(selectedMonth, selectedYear);
        model.addAllAttributes(dashboardStats);
        
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("availableYears", getAvailableYears());
        model.addAttribute("availableMonths", getAvailableMonths());
        model.addAttribute("activeSection", "dashboard");
        
        return "admin/admdashboard";
    }

    private String getCurrentMonthName() {
        return java.time.Month.from(java.time.LocalDate.now()).name();
    }

    private Map<String, String> getAvailableMonths() {
        Map<String, String> months = new LinkedHashMap<>();
        months.put("JANUARY", "January");
        months.put("FEBRUARY", "February");
        months.put("MARCH", "March");
        months.put("APRIL", "April");
        months.put("MAY", "May");
        months.put("JUNE", "June");
        months.put("JULY", "July");
        months.put("AUGUST", "August");
        months.put("SEPTEMBER", "September");
        months.put("OCTOBER", "October");
        months.put("NOVEMBER", "November");
        months.put("DECEMBER", "December");
        return months;
    }

    private List<String> getAvailableYears() {
        int currentYear = java.time.Year.now().getValue();
        List<String> years = new ArrayList<>();
        
        // Always show next year as the first option
        int startYear = currentYear + 1;
        
        for (int i = startYear; i >= startYear - 5; i--) {
            years.add(String.valueOf(i));
        }
        return years;
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        SecurityUtil.clearAllSessions(session);
        session.invalidate();
        return "redirect:/admin";
    }

    // =================== CATEGORY MANAGEMENT WITH PAGINATION ====================
    @GetMapping("/admin/category")
    public String showCategoryPage(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size,
        HttpSession session, 
        Model model) {
        
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        // Calculate offset
        int offset = (page - 1) * size;
        
        // Get paginated data
        List<CategoryBean> categories = categoryRepo.findAllWithPagination(offset, size);
        int totalItems = categoryRepo.countAllCategories();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryBean());
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("activeSection", "categories");
        model.addAttribute("admin", admin);
        
        return "admin/admdashboard";
    }

    @PostMapping("/admin/category/save")
    public String saveCategory(@ModelAttribute("category") CategoryBean category,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               HttpSession session,
                               RedirectAttributes ra) {

        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();

        System.out.println("DEBUG: Starting saveCategory method");
        System.out.println("DEBUG: Category ID: " + category.getId());
        System.out.println("DEBUG: Category Name: " + category.getCatName());

        // Check for duplicate category name (case-insensitive) - IGNORE SOFT-DELETED CATEGORIES
        String categoryName = category.getCatName().trim();
        boolean isDuplicate = categoryRepo.findAll().stream()
                .anyMatch(existingCat -> 
                    existingCat.getCatName().trim().equalsIgnoreCase(categoryName) &&
                    existingCat.getId() != category.getId() &&
                    !existingCat.isDeleted()); // ← ADD THIS LINE TO IGNORE SOFT-DELETED

        if (isDuplicate) {
            System.out.println("DEBUG: Duplicate category found: " + categoryName);
            ra.addFlashAttribute("errorMsg", "Category name '" + categoryName + "' already exists!");
            ra.addFlashAttribute("category", category);
            
            // Add pagination attributes to redirect
            ra.addAttribute("page", page);
            ra.addAttribute("size", size);
            return "redirect:/admin/category#category-section";
        }

        System.out.println("DEBUG: Admin ID: " + admin.getId());
        category.setAdminId(admin.getId());

        try {
            if (category.getId() == 0) {
                System.out.println("DEBUG: Creating new category");
                boolean saveResult = categoryRepo.save(category);
                System.out.println("DEBUG: Save result: " + saveResult);
                ra.addFlashAttribute("msg", "Category added successfully!");
            } else {
                System.out.println("DEBUG: Updating existing category");
                boolean updateResult = categoryRepo.update(category);
                System.out.println("DEBUG: Update result: " + updateResult);
                ra.addFlashAttribute("msg", "Category updated successfully!");
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error during category save/update: " + e.getMessage());
            e.printStackTrace();
            ra.addFlashAttribute("errorMsg", "Error saving category: " + e.getMessage());
            ra.addFlashAttribute("category", category);
            
            // Add pagination attributes to redirect
            ra.addAttribute("page", page);
            ra.addAttribute("size", size);
            return "redirect:/admin/category#category-section";
        }

        ra.addFlashAttribute("category", new CategoryBean());
        
        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        
        System.out.println("DEBUG: Category operation completed successfully");
        return "redirect:/admin/category#category-section";
    }
    
    
    @GetMapping("/admin/category/edit/{id}")
    public String editCategory(@PathVariable("id") int id, 
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(defaultValue = "5") int size,
                             HttpSession session,
                             RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        ra.addFlashAttribute("category", categoryRepo.findById(id).orElse(new CategoryBean()));
        
        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        
        return "redirect:/admin/category#category-section";
    }

    @GetMapping("/admin/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, 
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "5") int size,
                               HttpSession session,
                               RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        try {
            // 1. First check if category has ANY job posts (active or inactive)
            int totalJobCount = jobPostRepository.countAllJobsByCategory(id);
            
            if (totalJobCount > 0) {
                ra.addFlashAttribute("errorMsg", 
                    "Cannot delete category! There are " + totalJobCount + 
                    " job posts using this category. ");
            } else {
                // 2. Perform soft delete instead of hard delete
                boolean deleted = categoryRepo.softDelete(id, admin.getId());
                
                if (deleted) {
                    ra.addFlashAttribute("msg", "Category deleted successfully!");
                } else {
                    ra.addFlashAttribute("errorMsg", "Failed to delete category!");
                }
            }
            
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error deleting category: " + e.getMessage());
        }
        
        ra.addFlashAttribute("category", new CategoryBean());
        
        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        
        return "redirect:/admin/category#category-section";
    }
    
    
    
    @GetMapping("/admin/joblistings")
    public String showJobListings(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) String status,
        HttpSession session, 
        Model model) {
        
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        // Calculate offset
        int offset = (page - 1) * size;
        
        // Get paginated data based on status
        List<JobPostBean> jobListings;
        int totalItems;
        
        if (status != null && !status.isEmpty()) {
            // Filter by status
            jobListings = jobPVByAdmRepo.findByStatusWithPagination(status, offset, size);
            totalItems = jobPVByAdmRepo.countByStatus(status);
        } else {
            // Get all jobs
            jobListings = jobPVByAdmRepo.findAllWithPagination(offset, size);
            totalItems = jobPVByAdmRepo.countAllJobs();
        }
        
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        model.addAttribute("jobListings", jobListings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("activeSection", "joblistings");
        model.addAttribute("admin", admin);
        
        return "admin/admdashboard";
    }
    // =================== JOB DETAILS ENDPOINT ====================
 // =================== JOB DETAILS ENDPOINT ====================
    @GetMapping("/admin/job/details/{id}")
    @ResponseBody
    public ResponseEntity<?> getJobDetails(@PathVariable("id") Integer id, HttpSession session) {
        System.out.println("🔍 Getting job details for ID: " + id);
        
        try {
            // Security check - return JSON error if not authenticated
            Optional<AdminLoginBean> adminOpt = SecurityUtil.getCurrentAdmin(session);
            if (!adminOpt.isPresent()) {
                System.out.println("❌ Unauthorized access to job details");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized"));
            }
            
            // Get job from repository
            Optional<JobPostBean> jobOpt = jobPostRepository.findJobById(id);
            
            if (jobOpt.isPresent()) {
                JobPostBean job = jobOpt.get();
                System.out.println("✅ Found job: " + job.getJobTitle());
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", job.getId());
                response.put("jobTitle", job.getJobTitle());
                response.put("companyName", job.getCompanyName());
                response.put("jobType", job.getJobType());
                response.put("location", job.getLocation());
                response.put("department", job.getDepartment());
                response.put("experienceLevel", job.getExperienceLevel());
                response.put("educationLevel", job.getEducationLevel());
                response.put("salaryMini", job.getSalaryMini());
                response.put("salaryMax", job.getSalaryMax());
                response.put("negotiable", job.getNegotiable());
                response.put("jobDescription", job.getJobDescription());
                response.put("requiredSkills", job.getRequiredSkills());
                response.put("benefits", job.getBenefits());
                response.put("applicationDeadline", job.getApplicationDeadline());
                response.put("status", job.getStatus());
                
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Job not found for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Job not found"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
    @GetMapping("/admin/job/approve/{id}")
    public String approveJob(@PathVariable("id") Integer id, 
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size,
                           HttpSession session,
                           RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        try {
            boolean updated = jobPVByAdmRepo.updateJobStatus(id, "APPROVED");
            if (updated) {
                ra.addFlashAttribute("jobMsg", "Job approved successfully!");
            } else {
                ra.addFlashAttribute("jobErrorMsg", "Failed to approve job!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("jobErrorMsg", "Error approving job: " + e.getMessage());
        }
        
        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/joblistings#joblistings-section";
    }

    @GetMapping("/admin/job/reject/{id}")
    public String rejectJob(@PathVariable("id") Integer id, 
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "5") int size,
                          HttpSession session,
                          RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        boolean updated = jobPostRepository.updateJobStatus(id, "REJECTED");

        if (updated) {
            ra.addFlashAttribute("jobMsg", "Job rejected successfully!");
        } else {
            ra.addFlashAttribute("jobErrorMsg", "Failed to reject job!");
        }

        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/joblistings#joblistings-section";
    }

    @GetMapping("/admin/job/delete/{id}")
    public String deleteJob(@PathVariable("id") Integer id, 
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "5") int size,
                          HttpSession session,
                          RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        try {
            boolean deleted = jobPVByAdmRepo.deleteById(id);
            if (deleted) {
                ra.addFlashAttribute("jobMsg", "Job deleted successfully!");
            } else {
                ra.addFlashAttribute("jobErrorMsg", "Failed to delete job!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("jobErrorMsg", "Error deleting job: " + e.getMessage());
        }
        
        // Add pagination attributes to redirect
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/joblistings#joblistings-section";
    }
    
 // =================== OWNER REQUEST MANAGEMENT WITH PAGINATION ====================
    @GetMapping("/admin/owner-requests")
    public String showOwnerRequests(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(required = false) String status,
        HttpSession session, 
        Model model) {
        
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        int offset = dashboardService.calculateOffset(page, size);
        
        List<OwnerRequest> requests;
        int totalItems;
        
        if (status != null && !status.isEmpty()) {
            String dbStatus = status.toUpperCase();
            if ("pending".equalsIgnoreCase(status)) {
                dbStatus = "PENDING";
            }
            
            requests = ownerRequestRepository.findRequestsByStatusWithPagination(dbStatus, offset, size);
            totalItems = ownerRequestRepository.countRequestsByStatus(dbStatus);
        } else {
            requests = ownerRequestRepository.findAllRequestsWithPagination(offset, size);
            totalItems = ownerRequestRepository.countAllRequests();
        }
        
        int totalPages = dashboardService.calculateTotalPages(totalItems, size);
        List<Object> paginationNumbers = dashboardService.generatePagination(page, totalPages, 5);
        
        model.addAttribute("requests", requests);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("paginationNumbers", paginationNumbers);
        model.addAttribute("statusFilter", status);
        model.addAttribute("activeSection", "ownerrequests");
        model.addAttribute("admin", admin);
        
        return "admin/admdashboard";
    }
    
    @PostMapping("/admin/owner-request/approve/{requestId}")
    public String approveOwnerRequest(@PathVariable("requestId") int requestId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        // Get the request
        OwnerRequest request = null;
        try {
            Optional<OwnerRequest> requestOpt = ownerRequestRepository.findById(requestId);
            if (requestOpt.isPresent()) {
                request = requestOpt.get();
            } else {
                ra.addFlashAttribute("errorMsg", "Owner request not found!");
                ra.addAttribute("page", page);
                ra.addAttribute("size", size);
                return "redirect:/admin/owner-requests#ownerrequests-section";
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error finding owner request!");
            ra.addAttribute("page", page);
            ra.addAttribute("size", size);
            return "redirect:/admin/owner-requests#ownerrequests-section";
        }
        
        // Approve the request
        boolean success = ownerRequestRepository.approveRequest(requestId, admin.getId());
        
        if (success) {
            emailService.sendOwnerApprovalEmail(request.getGmail(), 
                request.getCompanyName(),
                request.getCompanyPhone(),
                request.getDescription(),
                request.getCity(),
                request.getTownship()  // Added township parameter
            );
            ra.addFlashAttribute("msg", "Owner request approved successfully! Registration link with pre-filled data sent to company email.");
        } else {
            ra.addFlashAttribute("errorMsg", "Failed to approve owner request!");
        }
        
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/owner-requests#ownerrequests-section";
    }
    
    @PostMapping("/admin/owner-request/reject/{requestId}")
    public String rejectOwnerRequest(@PathVariable("requestId") int requestId,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "5") int size,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        boolean success = ownerRequestRepository.rejectRequest(requestId, admin.getId());
        
        if (success) {
            ra.addFlashAttribute("msg", "Owner request rejected successfully!");
        } else {
            ra.addFlashAttribute("errorMsg", "Failed to reject owner request!");
        }
        
        ra.addAttribute("page", page);
        ra.addAttribute("size", size);
        return "redirect:/admin/owner-requests#ownerrequests-section";
    }
    
    @GetMapping("/admin/feedback")
    public String showFeedback(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "5") int size,
        HttpSession session, 
        Model model) {
        
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        // Calculate offset
        int offset = (page - 1) * size;
        
        // Get paginated data using service
        List<FeedBackBean> feedbacks = feedbackService.getFeedbacksWithPagination(offset, size);
        int totalItems = feedbackService.getTotalFeedbackCount();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("activeSection", "feedback");
        
        return "admin/admdashboard";
    }
    
    @GetMapping("/admin/job/{jobId}/owner-phone")
    @ResponseBody
    public ResponseEntity<String> getOwnerPhone(@PathVariable("jobId") Integer jobId, HttpSession session) {
        try {
            // Security check
            SecurityUtil.getCurrentAdmin(session).get();
            
            // Get job to find owner
            Optional<JobPostBean> jobOpt = jobPostRepository.findJobById(jobId);
            if (jobOpt.isPresent()) {
                Optional<Owner> ownerOpt = ownerRepository.findById(jobOpt.get().getOwnerId());
                if (ownerOpt.isPresent()) {
                    return ResponseEntity.ok(ownerOpt.get().getCompanyPhone());
                }
            }
            return ResponseEntity.ok("Not available");
        } catch (Exception e) {
            return ResponseEntity.ok("Error");
        }
    }
}
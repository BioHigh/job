package com.springboot.Job.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springboot.Job.model.AdminLoginBean;
import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.model.JobPostBean;
import com.springboot.Job.model.LoginBean;
import com.springboot.Job.repository.AdminLoginRepository;
import com.springboot.Job.repository.CategoryRepository;
import com.springboot.Job.repository.JobPVByAdmRepository;
import com.springboot.Job.repository.JobPostRepository;
import com.springboot.Job.util.SecurityUtil;

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
    public String showDashboard(HttpSession session, Model model) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        addDashboardStats(model);
        model.addAttribute("activeSection", "dashboard");
        return "admin/admdashboard"; // FIXED: Match actual file name
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        SecurityUtil.clearAllSessions(session);
        session.invalidate();
        return "redirect:/admin";
    }

    // =================== CATEGORY MANAGEMENT ====================
    @GetMapping("/admin/category")
    public String showCategoryPage(HttpSession session, Model model) {
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryBean());
        }
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("activeSection", "categories");
        model.addAttribute("admin", admin);
        addDashboardStats(model);
        return "admin/admdashboard"; // FIXED: Match actual file name
    }

    @PostMapping("/admin/category/save")
    public String saveCategory(@ModelAttribute("category") CategoryBean category,
                               HttpSession session,
                               RedirectAttributes ra) {

        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();

        System.out.println("DEBUG: Starting saveCategory method");
        System.out.println("DEBUG: Category ID: " + category.getId());
        System.out.println("DEBUG: Category Name: " + category.getCatName());

        // Check for duplicate category name (case-insensitive)
        String categoryName = category.getCatName().trim();
        boolean isDuplicate = categoryRepo.findAll().stream()
                .anyMatch(existingCat -> 
                    existingCat.getCatName().trim().equalsIgnoreCase(categoryName) &&
                    existingCat.getId() != category.getId());

        if (isDuplicate) {
            System.out.println("DEBUG: Duplicate category found: " + categoryName);
            ra.addFlashAttribute("errorMsg", "Category name '" + categoryName + "' already exists!");
            ra.addFlashAttribute("category", category);
            ra.addFlashAttribute("categories", categoryRepo.findAll());
            ra.addFlashAttribute("activeSection", "categories");
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
            ra.addFlashAttribute("categories", categoryRepo.findAll());
            ra.addFlashAttribute("activeSection", "categories");
            return "redirect:/admin/category#category-section";
        }

        ra.addFlashAttribute("category", new CategoryBean());
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        ra.addFlashAttribute("activeSection", "categories");
        
        System.out.println("DEBUG: Category operation completed successfully");
        return "redirect:/admin/category#category-section";
    }

    @GetMapping("/admin/category/edit/{id}")
    public String editCategory(@PathVariable("id") int id, 
                             HttpSession session,
                             RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        ra.addFlashAttribute("category", categoryRepo.findById(id).orElse(new CategoryBean()));
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        ra.addFlashAttribute("activeSection", "categories");
        return "redirect:/admin/category#category-section";
    }

    @GetMapping("/admin/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, 
                               HttpSession session,
                               RedirectAttributes ra) {
        // Security handled by interceptor - directly get admin
        SecurityUtil.getCurrentAdmin(session).get();
        
        categoryRepo.delete(id);
        
        ra.addFlashAttribute("msg", "Category deleted successfully!");
        ra.addFlashAttribute("category", new CategoryBean());
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        ra.addFlashAttribute("activeSection", "categories");
        return "redirect:/admin/category#category-section";
    }

    // =================== JOB LISTINGS MANAGEMENT ====================
    @GetMapping("/admin/joblistings")
    public String showJobListings(HttpSession session, Model model) {
        // Security handled by interceptor - directly get admin
        AdminLoginBean admin = SecurityUtil.getCurrentAdmin(session).get();
        
        List<JobPostBean> jobListings = jobPVByAdmRepo.findAll();
        model.addAttribute("jobListings", jobListings);
        model.addAttribute("activeSection", "joblistings");
        model.addAttribute("admin", admin);
        addDashboardStats(model);
        return "admin/admdashboard"; // FIXED: Match actual file name
    }

    @GetMapping("/admin/job/approve/{id}")
    public String approveJob(@PathVariable("id") Integer id, 
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
        return "redirect:/admin/joblistings#joblistings-section";
    }

    @GetMapping("/admin/job/reject/{id}")
    public String rejectJob(@PathVariable("id") Integer id, 
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

        return "redirect:/admin/joblistings#joblistings-section";
    }

    @GetMapping("/admin/job/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id, 
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
        return "redirect:/admin/joblistings#joblistings-section";
    }

    // =================== HELPER METHODS ====================
    private void addDashboardStats(Model model) {
        // Add your actual statistics here
        model.addAttribute("jobSeekerCount", jobPVByAdmRepo.countJobSeekers());
        model.addAttribute("employerCount", jobPVByAdmRepo.countEmployers());
        model.addAttribute("activeJobCount", jobPVByAdmRepo.countActiveJobs());
        model.addAttribute("applicationCount", jobPVByAdmRepo.countApplications());
    }
}
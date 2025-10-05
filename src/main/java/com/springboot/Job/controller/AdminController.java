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
import com.springboot.Job.model.LoginBean;
import com.springboot.Job.repository.AdminLoginRepository;
import com.springboot.Job.repository.CategoryRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminLoginRepository adminRepo;

    @Autowired
    private CategoryRepository categoryRepo;

    // =================== LOGIN ====================
    @GetMapping("/admin")
    public ModelAndView showadmLogin() {
        return new ModelAndView("admin/AdminLogin", "admlogin", new LoginBean());
    }

    @PostMapping("/admindashboard")
    public String dologin(@ModelAttribute("admlogin") LoginBean obj,
                          HttpSession session,
                          RedirectAttributes ra) {
        List<AdminLoginBean> adm = adminRepo.loginAdmin(obj);

        if (adm.isEmpty()) {
            ra.addFlashAttribute("msg", "Incorrect name and password!");
            return "redirect:/admin";
        } else {
            AdminLoginBean admin = adm.get(0);
            session.setAttribute("loginadm", admin);
            session.setAttribute("adminName", admin.getName());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("jobSeekerCount", 0);
        model.addAttribute("employerCount", 0);
        model.addAttribute("activeJobCount", 0);
        model.addAttribute("applicationCount", 0);
        
        // Remove showCategorySection or set to false for dashboard
        model.addAttribute("showCategorySection", false);

        return "admin/admdashboard";
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin";
    }

    // =================== CATEGORY MANAGEMENT ====================

    // Show category page - now with showCategorySection = true
    @GetMapping("/admin/category")
    public String showCategoryPage(Model model) {
        if (!model.containsAttribute("category")) {
            model.addAttribute("category", new CategoryBean());
        }
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("showCategorySection", true); // ✅ Show category section
        return "admin/admdashboard";  // ✅ Use the same dashboard template
    }

    @PostMapping("/admin/category/save")
    public String saveCategory(@ModelAttribute("category") CategoryBean category,
                               HttpSession session,
                               RedirectAttributes ra) {

        System.out.println("DEBUG: Starting saveCategory method");
        System.out.println("DEBUG: Category ID: " + category.getId());
        System.out.println("DEBUG: Category Name: " + category.getCatName());

        // Check for duplicate category name (case-insensitive)
        String categoryName = category.getCatName().trim();
        boolean isDuplicate = categoryRepo.findAll().stream()
                .anyMatch(existingCat -> 
                    existingCat.getCatName().trim().equalsIgnoreCase(categoryName) &&
                    existingCat.getId() != category.getId()); // Exclude current category during update

        if (isDuplicate) {
            System.out.println("DEBUG: Duplicate category found: " + categoryName);
            ra.addFlashAttribute("errorMsg", "Category name '" + categoryName + "' already exists!");
            ra.addFlashAttribute("category", category); // Keep the form data
            ra.addFlashAttribute("categories", categoryRepo.findAll());
            return "redirect:/admin/category#category-section";
        }

        // Debug: Check the admin session
        AdminLoginBean admin = (AdminLoginBean) session.getAttribute("loginadm");
        System.out.println("DEBUG: Admin from session: " + admin);
        
        if (admin != null) {
            System.out.println("DEBUG: Admin ID: " + admin.getId());
            System.out.println("DEBUG: Admin Name: " + admin.getName());
            System.out.println("DEBUG: Admin Email: " + admin.getGmail());
            category.setAdminId(admin.getId());
        } else {
            System.out.println("DEBUG: No admin found in session! Redirecting to login.");
            ra.addFlashAttribute("errorMsg", "You must be logged in to manage categories!");
            return "redirect:/admin";
        }

        System.out.println("DEBUG: Final Category Bean before save - ID: " + category.getId() + 
                          ", Name: " + category.getCatName() + 
                          ", Admin ID: " + category.getAdminId());

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
            return "redirect:/admin/category#category-section";
        }

        // Add category data for the page
        ra.addFlashAttribute("category", new CategoryBean()); // Reset form
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        
        System.out.println("DEBUG: Category operation completed successfully");
        return "redirect:/admin/category#category-section";
    }

    @GetMapping("/admin/category/edit/{id}")
    public String editCategory(@PathVariable("id") int id, RedirectAttributes ra) {
        ra.addFlashAttribute("category", categoryRepo.findById(id).orElse(new CategoryBean()));
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        return "redirect:/admin/category#category-section";
    }


        
    @GetMapping("/admin/category/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes ra) {
        categoryRepo.delete(id);
        
        ra.addFlashAttribute("msg", "Category deleted successfully!");
        ra.addFlashAttribute("category", new CategoryBean());
        ra.addFlashAttribute("categories", categoryRepo.findAll());
        return "redirect:/admin/category#category-section";
    }
    // =================== HELPER ====================
    @SuppressWarnings("unused")
	private void addDashboardStats(Model model) {
        model.addAttribute("jobSeekerCount", 0);
        model.addAttribute("employerCount", 0);
        model.addAttribute("activeJobCount", 0);
        model.addAttribute("applicationCount", 0);
    }
}
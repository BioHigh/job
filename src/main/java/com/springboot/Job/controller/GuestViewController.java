package com.springboot.Job.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.springboot.Job.service.JobPostService;
import com.springboot.Job.service.OwnerService;

import jakarta.servlet.http.HttpSession;

import com.springboot.Job.model.CategoryBean;
import com.springboot.Job.model.Owner;
import com.springboot.Job.service.CategoryService;


// fix in 15.10.2025
@Controller
public class GuestViewController {

    @Autowired
    private JobPostService jobPostService;
    
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OwnerService ownerService;
    
    @GetMapping("/")
    public String showIndex(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 4;
        long totalJobs = jobPostService.countAllJobs();
        List<Map<String, Object>> activeJobs = jobPostService.getActiveJobsWithOwners(page, pageSize);
        int totalPages = jobPostService.getTotalPages(pageSize);
        
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);
        
        return "index";
    }

    @GetMapping("/index")
    public String showIndex1(@RequestParam(defaultValue = "1") int page, Model model) {
        return showIndex(page, model);
    }
    
    @GetMapping("/jobs-by-category")
    public String showJobsByCategory(Model model) {
        try {
            List<CategoryBean> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "jobs-by-category";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("categories", List.of());
            return "jobs-by-category";
        }
    }
    
    @GetMapping("/api/jobs/category/{categoryId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getJobsByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            Map<String, Object> response = new HashMap<>();
            
           List<Map<String, Object>> jobs = jobPostService.getJobsByCategoryWithOwners(categoryId, page, size);
            long totalJobs = jobPostService.countJobsByCategory(categoryId);
            
            response.put("jobs", jobs);
            response.put("totalJobs", totalJobs);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) totalJobs / size));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to load jobs"));
        }
    }
    
    @GetMapping("/debug-categories")
    @ResponseBody
    public String debugCategories() {
        try {
            Object categories = categoryService.getCategoriesWithJobCountsForView();
            
            StringBuilder debug = new StringBuilder();
            debug.append("Data type: ").append(categories.getClass().getName()).append("\n\n");
            
            if (categories instanceof List) {
                List<?> categoryList = (List<?>) categories;
                debug.append("List size: ").append(categoryList.size()).append("\n\n");
                
                for (Object item : categoryList) {
                    debug.append("Item type: ").append(item.getClass().getName()).append("\n");
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        debug.append("Map contents: ").append(map).append("\n");
                    } else {
                        debug.append("Item: ").append(item).append("\n");
                    }
                    debug.append("---\n");
                }
            }
            
            return debug.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    //4-11-25 Pagination update

    @GetMapping("/user/guestview_companies") 
    public String showGuestViewCompanies(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            Model model) {
        
        System.out.println("=== Guest view companies accessed ===");
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
        
        return "guestview_companies";
    }
}
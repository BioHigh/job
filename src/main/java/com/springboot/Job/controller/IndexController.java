package com.springboot.Job.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.Job.service.JobPostService;

import java.util.List;
import java.util.Map;

//12.10.2025
@Controller
public class IndexController {
    
    @Autowired
    private JobPostService jobPostService;
    
    @GetMapping("/home")
    public String showHome(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 4; // Show 4 jobs per page
        long totalJobs = jobPostService.countAllJobs();
        List<Map<String, Object>> activeJobs = jobPostService.getActiveJobsWithOwners(page, pageSize);
        int totalPages = jobPostService.getTotalPages(pageSize);
        
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("hasPrev", page > 1);
        
        return "home";
    }
    
    @GetMapping("/company")
    public String showIndex3() {
        return "owner/company";
    }
}
package com.springboot.Job.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.Job.service.JobPostService;

// add in 11.10.2025

@Controller
public class GuestViewController {

    @Autowired
    private JobPostService jobPostService;
    
    @GetMapping("/")
    public String showIndex(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 4; // Show 4 jobs per page
        long totalJobs = jobPostService.countAllJobs(); // Use existing method
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
    
    
}
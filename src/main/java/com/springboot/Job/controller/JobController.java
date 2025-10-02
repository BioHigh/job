package com.springboot.Job.controller;

import com.springboot.Job.model.JobBean;
import com.springboot.Job.model.JobBean.Status;
import com.springboot.Job.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/owner/job")
public class JobController {

    @Autowired
    private JobRepository jobRepo;

 // Show Job Posting Form
    @GetMapping("/new")
    public String showJobForm(Model model) {
        model.addAttribute("job", new JobBean());
        return "owner/jobpost"; // must exist in templates/owner/jobpost.html
    }


    // Handle Job Form Submission
    @PostMapping("/save")
    public String saveJob(@ModelAttribute("job") JobBean job) {
        job.setStatus(Status.PENDING);       // default status
        job.setPostDate(LocalDateTime.now()); // set current time
        jobRepo.save(job);
        return "redirect:/owner/job/list"; // redirect to job list
    }

    // Show All Jobs for owner
    @GetMapping("/list")
    public String listJobs(Model model) {
        model.addAttribute("jobs", jobRepo.findAll());
        return "owner/job_list"; // templates/owner/job_list.html
    }
}

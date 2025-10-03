package com.springboot.Job.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.JobPost;

@Repository
public class JobPostRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean createJobPost(JobPost jobPost) {
        String sql = "INSERT INTO job.job_post (" +
                    "job_title, job_type, department, location, job_description, " +
                    "company_name, company_website, company_description, required_skills, " +
                    "experience_level, education_level, salary_min, salary_max, benefits, " +
                    "application_email, application_deadline, application_instructions, owner_id, status" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            System.out.println("Attempting to insert job post: " + jobPost.getJobTitle());
            
            int result = jdbcTemplate.update(sql,
                jobPost.getJobTitle(),
                jobPost.getJobType(),
                jobPost.getDepartment(),
                jobPost.getLocation(),
                jobPost.getJobDescription(),
                jobPost.getCompanyName(),
                jobPost.getCompanyWebsite(),
                jobPost.getCompanyDescription(),
                jobPost.getRequiredSkills(),
                jobPost.getExperienceLevel(),
                jobPost.getEducationLevel(),
                jobPost.getSalaryMin(),
                jobPost.getSalaryMax(),
                jobPost.getBenefits(),
                jobPost.getApplicationEmail(),
                jobPost.getApplicationDeadline(),
                jobPost.getApplicationInstructions(),
                jobPost.getOwnerId(),
                "PENDING"
            );
            
            System.out.println("Insert result: " + result);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error inserting job post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
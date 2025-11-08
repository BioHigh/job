package com.springboot.Job.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.RowMapper;
import com.springboot.Job.model.JobPostBean;

public class JobPostRowMapper implements RowMapper<JobPostBean> {
    
    @Override
    public JobPostBean mapRow(ResultSet rs, int rowNum) throws SQLException {
        JobPostBean jobPost = new JobPostBean();
        
        // Basic job information
        jobPost.setId(rs.getInt("id"));
        jobPost.setJobTitle(rs.getString("job_title"));
        jobPost.setJobType(rs.getString("job_type"));
        jobPost.setDepartment(rs.getString("department"));
        jobPost.setLocation(rs.getString("location"));
        jobPost.setJobDescription(rs.getString("job_description"));
        jobPost.setCompanyName(rs.getString("company_name"));
        jobPost.setCompanyWebsite(rs.getString("company_website"));
        jobPost.setCompanyDescription(rs.getString("company_description"));
        jobPost.setRequiredSkills(rs.getString("required_skills"));
        jobPost.setExperienceLevel(rs.getString("experience_level"));
        jobPost.setEducationLevel(rs.getString("education_level"));
        jobPost.setSalaryMini(rs.getInt("salary_mini"));
        jobPost.setSalaryMax(rs.getInt("salary_max"));
        
        // 🔥 CRITICAL: Add this missing line!
        jobPost.setNegotiable(rs.getString("negotiable"));
        
        jobPost.setBenefits(rs.getString("benefits"));
        jobPost.setApplicationEmail(rs.getString("application_email"));
        
        // Handle application deadline
        String deadlineStr = rs.getString("application_deadline");
        if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
            try {
                jobPost.setApplicationDeadline(LocalDate.parse(deadlineStr));
            } catch (Exception e) {
                // If parsing fails, leave it null
                System.err.println("Error parsing deadline: " + deadlineStr);
            }
        }
        
        jobPost.setApplicationInstructions(rs.getString("application_instructions"));
        jobPost.setOwnerId(rs.getInt("owner_id"));
        jobPost.setCategoryId(rs.getInt("category_id"));
        jobPost.setStatus(rs.getString("status"));
        
        // Handle timestamps
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            jobPost.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return jobPost;
    }
}
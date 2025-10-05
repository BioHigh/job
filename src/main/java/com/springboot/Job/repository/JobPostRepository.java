package com.springboot.Job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.JobPostBean;

@Repository
public class JobPostRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public boolean createJobPost(JobPostBean jobPost) {
        try {
            String sql = "INSERT INTO job_post (" +
                "job_title, job_type, department, location, job_description, " +
                "company_name, company_website, company_description, required_skills, " +
                "experience_level, education_level, salary_mini, salary_max, benefits, " +
                "application_email, application_deadline, application_instructions, " +
                "owner_id, status, category_id" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            System.out.println("Executing SQL: " + sql);
            
            int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws SQLException {
                    ps.setString(1, jobPost.getJobTitle());
                    ps.setString(2, jobPost.getJobType());
                    ps.setString(3, jobPost.getDepartment());
                    ps.setString(4, jobPost.getLocation());
                    ps.setString(5, jobPost.getJobDescription());
                    ps.setString(6, jobPost.getCompanyName());
                    ps.setString(7, jobPost.getCompanyWebsite());
                    ps.setString(8, jobPost.getCompanyDescription());
                    ps.setString(9, jobPost.getRequiredSkills());
                    ps.setString(10, jobPost.getExperienceLevel());
                    ps.setString(11, jobPost.getEducationLevel());
                    
                    if (jobPost.getSalaryMini() != null) {
                        ps.setInt(12, jobPost.getSalaryMini());
                    } else {
                        ps.setNull(12, Types.INTEGER);
                    }
                    
                    if (jobPost.getSalaryMax() != null) {
                        ps.setInt(13, jobPost.getSalaryMax());
                    } else {
                        ps.setNull(13, Types.INTEGER);
                    }
                    
                    ps.setString(14, jobPost.getBenefits());
                    ps.setString(15, jobPost.getApplicationEmail());
                    ps.setString(16, jobPost.getApplicationDeadline());
                    ps.setString(17, jobPost.getApplicationInstructions());
                    ps.setInt(18, jobPost.getOwnerId());
                    ps.setString(19, "PENDING");
                    
                    // Set category_id - this is the crucial fix
                    if (jobPost.getCategoryId() != null) {
                        ps.setInt(20, jobPost.getCategoryId());
                    } else {
                        // If no category is selected, you might want to set a default category
                        // or handle it differently based on your business logic
                        ps.setNull(20, Types.INTEGER);
                    }
                }
            });

            System.out.println("Insert result: " + result + " rows affected");
            return result > 0;
            
        } catch (DataAccessException e) {
            System.err.println("DataAccessException while creating job post: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Exception while creating job post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<JobPostBean> findRecentJobsByOwner(Integer ownerId, int limit) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? ORDER BY created_at DESC LIMIT ?";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId, limit);
        } catch (Exception e) {
            System.err.println("Error finding recent jobs: " + e.getMessage());
            return List.of(); // Return empty list on error
        }
    }

    public long countActiveJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE owner_id = ? AND status = 'ACTIVE'";
            return jdbcTemplate.queryForObject(sql, Long.class, ownerId);
        } catch (Exception e) {
            System.err.println("Error counting active jobs: " + e.getMessage());
            return 0;
        }
    }

    public long countTotalApplicationsByOwner(Integer ownerId) {
        try {
            // If you have an applications table, use this:
            // String sql = "SELECT COUNT(*) FROM applications a JOIN job_post j ON a.job_post_id = j.id WHERE j.owner_id = ?";
            // For now, return 0 or a default value
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting applications: " + e.getMessage());
            return 0;
        }
    }

    public long countTotalInterviewsByOwner(Integer ownerId) {
        try {
            // If you have an interviews table, use this:
            // String sql = "SELECT COUNT(*) FROM interviews i JOIN job_post j ON i.job_post_id = j.id WHERE j.owner_id = ?";
            // For now, return 0 or a default value
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting interviews: " + e.getMessage());
            return 0;
        }
    }
    
}
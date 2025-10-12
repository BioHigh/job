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
public class JobPVByAdmRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // =================== ADMIN METHODS ====================
    
    // Find all job posts for admin
    public List<JobPostBean> findAll() {
        try {
            String sql = "SELECT * FROM job_post ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class));
        } catch (Exception e) {
            System.err.println("Error finding all job posts: " + e.getMessage());
            return List.of();
        }
    }

    // Find job post by ID
    public JobPostBean findById(Integer id) {
        try {
            String sql = "SELECT * FROM job_post WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(JobPostBean.class), id);
        } catch (Exception e) {
            System.err.println("Error finding job post by ID: " + e.getMessage());
            return null;
        }
    }

    // Update job status (APPROVE/REJECT)
    public boolean updateJobStatus(Integer id, String status) {
        try {
            String sql = "UPDATE job_post SET status = ? WHERE id = ?";
            int result = jdbcTemplate.update(sql, status, id);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error updating job status: " + e.getMessage());
            return false;
        }
    }

    // Delete job post by ID
    public boolean deleteById(Long id) {
        try {
            String sql = "DELETE FROM job_post WHERE id = ?";
            int result = jdbcTemplate.update(sql, id);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error deleting job post: " + e.getMessage());
            return false;
        }
    }

    // Count methods for dashboard statistics
    public int countJobSeekers() {
        try {
            String sql = "SELECT COUNT(*) FROM user"; // Adjust table name as needed
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Error counting job seekers: " + e.getMessage());
            return 0;
        }
    }

    public int countEmployers() {
        try {
            String sql = "SELECT COUNT(*) FROM owner"; // Adjust table name as needed
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Error counting employers: " + e.getMessage());
            return 0;
        }
    }

    public int countActiveJobs() {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE status = 'APPROVED'";
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Error counting active jobs: " + e.getMessage());
            return 0;
        }
    }

    public int countApplications() {
        try {
            String sql = "SELECT COUNT(*) FROM job_application"; // Adjust table name as needed
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Error counting applications: " + e.getMessage());
            return 0;
        }
    }

    public int countPendingJobs() {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE status = 'PENDING'";
            return jdbcTemplate.queryForObject(sql, Integer.class);
        } catch (Exception e) {
            System.err.println("Error counting pending jobs: " + e.getMessage());
            return 0;
        }
    }

    // =================== EXISTING OWNER METHODS ====================

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
                    if (jobPost.getApplicationDeadline() != null) {
                        ps.setDate(16, java.sql.Date.valueOf(jobPost.getApplicationDeadline()));
                    } else {
                        ps.setNull(16, Types.DATE);
                    }

                    ps.setString(17, jobPost.getApplicationInstructions());
                    ps.setInt(18, jobPost.getOwnerId());
                    ps.setString(19, "PENDING");
                    
                    if (jobPost.getCategoryId() != null) {
                        ps.setInt(20, jobPost.getCategoryId());
                    } else {
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
            return List.of();
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
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting interviews: " + e.getMessage());
            return 0;
        }
    }
}
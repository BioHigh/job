package com.springboot.Job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

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
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting applications: " + e.getMessage());
            return 0;
        }
    }

    public long countTotalInterviewsByOwner(Integer ownerId) {
        try {
            return 0;
        } catch (Exception e) {
            System.err.println("Error counting interviews: " + e.getMessage());
            return 0;
        }
    }
    
    public List<JobPostBean> findAllJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId);
        } catch (Exception e) {
            System.err.println("Error finding all jobs by owner: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<JobPostBean> findByIdAndOwnerId(Integer jobId, Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE id = ? AND owner_id = ?";
            JobPostBean jobPost = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(JobPostBean.class), jobId, ownerId);
            return Optional.ofNullable(jobPost);
        } catch (Exception e) {
            System.err.println("Error finding job by ID and owner: " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean updateJobPost(JobPostBean jobPost) {
        try {
            String sql = "UPDATE job_post SET " +
                "job_title = ?, job_type = ?, department = ?, location = ?, " +
                "job_description = ?, company_name = ?, company_website = ?, " +
                "company_description = ?, required_skills = ?, experience_level = ?, " +
                "education_level = ?, salary_mini = ?, salary_max = ?, benefits = ?, " +
                "application_email = ?, application_deadline = ?, " +
                "application_instructions = ?, category_id = ?, status = ? " +
                "WHERE id = ? AND owner_id = ?";

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
                jobPost.getSalaryMini(),
                jobPost.getSalaryMax(),
                jobPost.getBenefits(),
                jobPost.getApplicationEmail(),
                jobPost.getApplicationDeadline(),
                jobPost.getApplicationInstructions(),
                jobPost.getCategoryId(),
                jobPost.getStatus(),
                jobPost.getId(),
                jobPost.getOwnerId()
            );

            return result > 0;
        } catch (Exception e) {
            System.err.println("Error updating job post: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteJobPost(Integer jobId, Integer ownerId) {
        try {
            String sql = "DELETE FROM job_post WHERE id = ? AND owner_id = ?";
            int result = jdbcTemplate.update(sql, jobId, ownerId);
            return result > 0;
        } catch (Exception e) {
            System.err.println("Error deleting job post: " + e.getMessage());
            return false;
        }
    }
    
    public List<JobPostBean> findByOwnerId(Integer ownerId) {
        String sql = "SELECT * FROM job_post WHERE owner_id = ? AND status = 'ACTIVE'";
        return jdbcTemplate.query(sql, new JobPostRowMapper(), ownerId);
    }
    
}
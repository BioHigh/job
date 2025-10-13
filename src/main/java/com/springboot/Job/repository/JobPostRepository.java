package com.springboot.Job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
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

    // -------------------- CREATE JOB --------------------
    public boolean createJobPost(JobPostBean jobPost) {
        try {
            String sql = "INSERT INTO job_post (" +
                    "job_title, job_type, department, location, job_description, " +
                    "company_name, company_website, company_description, required_skills, " +
                    "experience_level, education_level, salary_mini, salary_max, benefits, " +
                    "application_email, application_deadline, application_instructions, " +
                    "owner_id, status, category_id, is_archived" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)";

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

                    if (jobPost.getSalaryMini() != null) ps.setInt(12, jobPost.getSalaryMini());
                    else ps.setNull(12, Types.INTEGER);

                    if (jobPost.getSalaryMax() != null) ps.setInt(13, jobPost.getSalaryMax());
                    else ps.setNull(13, Types.INTEGER);

                    ps.setString(14, jobPost.getBenefits());
                    ps.setString(15, jobPost.getApplicationEmail());

                    if (jobPost.getApplicationDeadline() != null)
                        ps.setDate(16, java.sql.Date.valueOf(jobPost.getApplicationDeadline()));
                    else ps.setNull(16, Types.DATE);

                    ps.setString(17, jobPost.getApplicationInstructions());
                    ps.setInt(18, jobPost.getOwnerId());
                    ps.setString(19, "PENDING");

                    if (jobPost.getCategoryId() != null) ps.setInt(20, jobPost.getCategoryId());
                    else ps.setNull(20, Types.INTEGER);
                }
            });

            return result > 0;

        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- READ JOBS --------------------
    public List<JobPostBean> findByOwnerId(Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<JobPostBean> findAllJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<JobPostBean> findRecentJobsByOwner(Integer ownerId, int limit) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? AND is_archived = FALSE ORDER BY created_at DESC LIMIT ?";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId, limit);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<JobPostBean> findActiveJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? AND is_archived = FALSE ORDER BY created_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<JobPostBean> findArchivedJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE owner_id = ? AND is_archived = TRUE ORDER BY archived_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class), ownerId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Optional<JobPostBean> findByIdAndOwnerId(Integer jobId, Integer ownerId) {
        try {
            String sql = "SELECT * FROM job_post WHERE id = ? AND owner_id = ?";
            JobPostBean jobPost = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(JobPostBean.class), jobId, ownerId);
            return Optional.ofNullable(jobPost);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<JobPostBean> findJobById(Integer jobId) {
        try {
            String sql = "SELECT * FROM job_post WHERE id = ?";
            JobPostBean jobPost = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(JobPostBean.class), jobId);
            return Optional.ofNullable(jobPost);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // -------------------- UPDATE JOB --------------------
    public boolean updateJobPost(JobPostBean jobPost) {
        try {
            String sql = "UPDATE job_post SET " +
                    "job_title = ?, job_type = ?, department = ?, location = ?, " +
                    "job_description = ?, company_name = ?, company_website = ?, " +
                    "company_description = ?, required_skills = ?, experience_level = ?, " +
                    "education_level = ?, salary_mini = ?, salary_max = ?, benefits = ?, " +
                    "application_email = ?, application_deadline = ?, " +
                    "application_instructions = ?, category_id = ?, status = ?, " +
                    "is_archived = ?, archived_at = ?, archived_by = ?, archive_reason = ? " +
                    "WHERE id = ? AND owner_id = ?";
            
            int result = jdbcTemplate.update(sql,
                    jobPost.getJobTitle(), jobPost.getJobType(), jobPost.getDepartment(), jobPost.getLocation(),
                    jobPost.getJobDescription(), jobPost.getCompanyName(), jobPost.getCompanyWebsite(),
                    jobPost.getCompanyDescription(), jobPost.getRequiredSkills(), jobPost.getExperienceLevel(),
                    jobPost.getEducationLevel(), jobPost.getSalaryMini(), jobPost.getSalaryMax(),
                    jobPost.getBenefits(), jobPost.getApplicationEmail(), jobPost.getApplicationDeadline(),
                    jobPost.getApplicationInstructions(), jobPost.getCategoryId(), jobPost.getStatus(),
                    jobPost.getIsArchived(), jobPost.getArchivedAt(), jobPost.getArchivedBy(), jobPost.getArchiveReason(),
                    jobPost.getId(), jobPost.getOwnerId());
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateJobStatus(Integer jobId, String status) {
        try {
            String sql = "UPDATE job_post SET status = ? WHERE id = ?";
            int rows = jdbcTemplate.update(sql, status, jobId);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- DELETE JOB --------------------
    public boolean deleteJobPost(Integer jobId) {
        try {
            String sql = "DELETE FROM job_post WHERE id = ?";
            int result = jdbcTemplate.update(sql, jobId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteJobPost(Integer jobId, Integer ownerId) {
        try {
            String sql = "DELETE FROM job_post WHERE id = ? AND owner_id = ?";
            int result = jdbcTemplate.update(sql, jobId, ownerId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- ARCHIVE & RESTORE --------------------
    public boolean archiveJob(Integer jobId, String archivedBy, String reason) {
        try {
            String sql = "UPDATE job_post SET is_archived = TRUE, archived_at = NOW(), archived_by = ?, archive_reason = ? WHERE id = ?";
            int rows = jdbcTemplate.update(sql, archivedBy, reason, jobId);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreJob(Integer jobId) {
        try {
            String sql = "UPDATE job_post SET is_archived = FALSE, archived_at = NULL, archived_by = NULL, archive_reason = NULL WHERE id = ?";
            int rows = jdbcTemplate.update(sql, jobId);
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------- COUNT & OTHER QUERIES --------------------
    public long countActiveJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE owner_id = ? AND status = 'APPROVED' AND is_archived = FALSE";
            return jdbcTemplate.queryForObject(sql, Long.class, ownerId);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countArchivedJobsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE owner_id = ? AND is_archived = TRUE";
            return jdbcTemplate.queryForObject(sql, Long.class, ownerId);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countTotalApplicationsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT COUNT(*) FROM job_application ja " +
                        "JOIN job_post jp ON ja.job_post_id = jp.id " +
                        "WHERE jp.owner_id = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, ownerId);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countTotalInterviewsByOwner(Integer ownerId) {
        try {
            String sql = "SELECT COUNT(*) FROM interviews i " +
                        "JOIN job_application ja ON i.application_id = ja.id " +
                        "JOIN job_post jp ON ja.job_post_id = jp.id " +
                        "WHERE jp.owner_id = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, ownerId);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countAllJobs() {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE status = 'APPROVED' AND is_archived = FALSE";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countAllArchivedJobs() {
        try {
            String sql = "SELECT COUNT(*) FROM job_post WHERE is_archived = TRUE";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<JobPostBean> findAllArchivedJobs() {
        try {
            String sql = "SELECT * FROM job_post WHERE is_archived = TRUE ORDER BY archived_at DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobPostBean.class));
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Map<String, Object>> findActiveJobsWithOwners(int limit, int offset) {
        try {
            String sql = "SELECT jp.id, jp.job_title as jobTitle, jp.job_type as jobType, " +
                    "jp.location, jp.company_name as companyName, jp.department, " +
                    "jp.experience_level as experienceLevel, jp.created_at as createdAt, " +
                    "o.profile_photo as profilePhoto, o.company_name as ownerCompanyName " +
                    "FROM job_post jp " +
                    "LEFT JOIN owner o ON jp.owner_id = o.id " +
                    "WHERE jp.status = 'APPROVED' AND jp.is_archived = FALSE " +
                    "ORDER BY jp.created_at DESC " +
                    "LIMIT ? OFFSET ?";
            return jdbcTemplate.queryForList(sql, limit, offset);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}
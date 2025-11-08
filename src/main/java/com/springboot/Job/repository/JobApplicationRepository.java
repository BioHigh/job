package com.springboot.Job.repository;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.JobApplicationBean;

@Repository
public class JobApplicationRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Save job application - FIXED
    public int saveJobApplication(JobApplicationBean app) {
        String sql = "INSERT INTO job_application (user_id, job_id, cv, status, apply_date, is_seen) VALUES (?, ?, ?, 'PENDING', NOW(), FALSE)";
        
        try {
            System.out.println("=== DEBUG: Saving Job Application ===");
            System.out.println("User ID: " + app.getUserId());
            System.out.println("Job ID: " + app.getJobId());
            System.out.println("CV File: " + (app.getCvFile() != null ? "Present, size: " + app.getCvFile().length + " bytes" : "NULL"));
            
            int result = jdbcTemplate.update(sql, new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps) throws java.sql.SQLException {
                    ps.setInt(1, app.getUserId());
                    ps.setInt(2, app.getJobId());
                    
                    if (app.getCvFile() != null && app.getCvFile().length > 0) {
                        ps.setBytes(3, app.getCvFile());
                        System.out.println("✅ CV file set in prepared statement");
                    } else {
                        ps.setNull(3, java.sql.Types.BLOB);
                        System.out.println("⚠️ No CV file - setting NULL");
                    }
                }
            });
            
            System.out.println("📊 Database result: " + result + " row(s) affected");
            System.out.println("=== DEBUG END ===");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in saveJobApplication:");
            e.printStackTrace();
            return 0;
        }
    }

    // Get applications with user details for owner - FIXED
    public List<JobApplicationBean> findApplicationsWithUserDetails(int ownerId) {
        try {
            String sql = """
                SELECT 
                    ja.id, ja.user_id, ja.job_id, ja.cv as cvFile, ja.apply_date, ja.status, ja.is_seen,
                    u.name as user_name, u.gmail as user_email, u.phone as user_phone,
                    jp.title as job_title, o.company_name
                FROM job_application ja
                JOIN job_post jp ON ja.job_id = jp.id
                JOIN user u ON ja.user_id = u.id
                JOIN owner o ON jp.owner_id = o.id
                WHERE jp.owner_id = ?
                ORDER BY ja.apply_date DESC
                """;
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplicationBean.class), ownerId);
        } catch (Exception e) {
            System.err.println("Error in findApplicationsWithUserDetails: " + e.getMessage());
            // Fallback to basic query
            return findByOwnerId(ownerId);
        }
    }

    // Get user name
    public String getUserName(int userId) {
        try {
            String sql = "SELECT name FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            return "User #" + userId;
        }
    }

    // Get user email
    public String getUserEmail(int userId) {
        try {
            String sql = "SELECT gmail FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    // Get user phone
    public String getUserPhone(int userId) {
        try {
            String sql = "SELECT phone FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (Exception e) {
            return null;
        }
    }

    // Existing methods
    public List<JobApplicationBean> findByJobId(int jobId) {
        try {
            String sql = "SELECT id, user_id, job_id, cv as cvFile, apply_date, status, is_seen FROM job_application WHERE job_id = ? ORDER BY apply_date DESC";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplicationBean.class), jobId);
        } catch (Exception e) {
            System.err.println("Error in findByJobId: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<JobApplicationBean> findById(int id) {
        try {
            String sql = "SELECT id, user_id, job_id, cv as cvFile, apply_date, status, is_seen FROM job_application WHERE id = ?";
            List<JobApplicationBean> applications = jdbcTemplate.query(
                sql, new BeanPropertyRowMapper<>(JobApplicationBean.class), id);
            return applications.isEmpty() ? Optional.empty() : Optional.of(applications.get(0));
        } catch (Exception e) {
            System.err.println("Error in findById: " + e.getMessage());
            return Optional.empty();
        }
    }

    // Find by owner ID - FIXED
    public List<JobApplicationBean> findByOwnerId(int ownerId) {
        try {
            String sql = """
                SELECT ja.id, ja.user_id, ja.job_id, ja.cv as cvFile, ja.apply_date, ja.status, ja.is_seen 
                FROM job_application ja
                JOIN job_post jp ON ja.job_id = jp.id
                WHERE jp.owner_id = ?
                ORDER BY ja.apply_date DESC
                """;
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(JobApplicationBean.class), ownerId);
        } catch (Exception e) {
            System.err.println("Error in findByOwnerId: " + e.getMessage());
            return List.of();
        }
    }

    public boolean updateApplicationStatus(int applicationId, String status) {
        String sql = "UPDATE job_application SET status = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, status, applicationId);
        return updated > 0;
    }

    public byte[] getCvByApplicationId(int applicationId) {
        String sql = "SELECT cv FROM job_application WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, byte[].class, applicationId);
        } catch (Exception e) {
            return null;
        }
    }
    
    // Count UNSEEN applications for owner
    public int countUnseenApplicationsByOwnerId(int ownerId) {
        try {
            String sql = """
                SELECT COUNT(ja.id) 
                FROM job_application ja
                JOIN job_post jp ON ja.job_id = jp.id 
                WHERE jp.owner_id = ? AND ja.is_seen = FALSE
                """;
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ownerId);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error in countUnseenApplicationsByOwnerId: " + e.getMessage());
            return 0;
        }
    }

    // Mark application as seen
    public boolean markApplicationAsSeen(int applicationId) {
        try {
            String sql = "UPDATE job_application SET is_seen = TRUE WHERE id = ?";
            int updated = jdbcTemplate.update(sql, applicationId);
            return updated > 0;
        } catch (Exception e) {
            System.err.println("Error in markApplicationAsSeen: " + e.getMessage());
            return false;
        }
    }

    // Mark all applications as seen for owner
    public boolean markAllApplicationsAsSeen(int ownerId) {
        try {
            String sql = """
                UPDATE job_application ja
                JOIN job_post jp ON ja.job_id = jp.id
                SET ja.is_seen = TRUE 
                WHERE jp.owner_id = ? AND ja.is_seen = FALSE
                """;
            int updated = jdbcTemplate.update(sql, ownerId);
            return updated > 0;
        } catch (Exception e) {
            System.err.println("Error in markAllApplicationsAsSeen: " + e.getMessage());
            return false;
        }
    }
    
    // Get user applications - FIXED
    public List<Map<String, Object>> getUserApplications(int userId) {
        String sql = "SELECT ja.*, jp.title as job_title, jp.job_type, jp.location, o.company_name, o.profile_photo as logo " +
                     "FROM job_application ja " +
                     "JOIN job_post jp ON ja.job_id = jp.id " +
                     "JOIN owner o ON jp.owner_id = o.id " +
                     "WHERE ja.user_id = ? " +
                     "ORDER BY ja.apply_date DESC";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", rs.getInt("id"));
            result.put("user_id", rs.getInt("user_id"));
            result.put("job_id", rs.getInt("job_id"));
            result.put("apply_date", rs.getTimestamp("apply_date"));
            result.put("status", rs.getString("status"));
            result.put("job_title", rs.getString("job_title"));
            result.put("job_type", rs.getString("job_type"));
            result.put("location", rs.getString("location"));
            result.put("company_name", rs.getString("company_name"));
            result.put("logo", rs.getBytes("logo"));
            
            // Add CV data check
            byte[] cv = rs.getBytes("cv");
            result.put("cv", cv);
            result.put("has_cv", cv != null);
            
            return result;
        }, userId);
    }

    // Get application by ID - FIXED
    public Map<String, Object> getApplicationById(int applicationId) {
        String sql = "SELECT ja.*, jp.title as job_title, jp.job_type, jp.location, o.company_name " +
                     "FROM job_application ja " +
                     "JOIN job_post jp ON ja.job_id = jp.id " +
                     "JOIN owner o ON jp.owner_id = o.id " +
                     "WHERE ja.id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", rs.getInt("id"));
                result.put("user_id", rs.getInt("user_id"));
                result.put("job_id", rs.getInt("job_id"));
                result.put("apply_date", rs.getTimestamp("apply_date"));
                result.put("status", rs.getString("status"));
                result.put("job_title", rs.getString("job_title"));
                result.put("job_type", rs.getString("job_type"));
                result.put("location", rs.getString("location"));
                result.put("company_name", rs.getString("company_name"));
                result.put("cv", rs.getBytes("cv"));
                return result;
            }, applicationId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Get CV data - FIXED
    public byte[] getCVData(int applicationId) {
        String sql = "SELECT cv FROM job_application WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, byte[].class, applicationId);
        } catch (Exception e) {
            return null;
        }
    }

    // Get application stats - FIXED
    public Map<String, Long> getApplicationStats(int userId) {
        String sql = "SELECT status, COUNT(*) as count FROM job_application WHERE user_id = ? GROUP BY status";
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, userId);
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("PENDING", 0L);
        stats.put("ACCEPTED", 0L);
        stats.put("REVIEWED", 0L);
        stats.put("REJECTED", 0L);
        stats.put("TOTAL", 0L);
        
        for (Map<String, Object> row : results) {
            String status = (String) row.get("status");
            Long count = ((Number) row.get("count")).longValue();
            stats.put(status, count);
        }
        
        // Total count
        String totalSql = "SELECT COUNT(*) as total FROM job_application WHERE user_id = ?";
        Long totalCount = jdbcTemplate.queryForObject(totalSql, Long.class, userId);
        stats.put("TOTAL", totalCount != null ? totalCount : 0L);
        
        return stats;
    }

    // Get user applications paginated - FIXED (using correct column names)
    public List<Map<String, Object>> getUserApplicationsPaginated(int userId, int page, int pageSize) {
        String sql = "SELECT ja.id, ja.apply_date, ja.status, ja.cv, " +
                     "jp.title as job_title, jp.job_type, jp.location, " +
                     "o.company_name " +
                     "FROM job_application ja " +
                     "JOIN job_post jp ON ja.job_id = jp.id " +
                     "JOIN owner o ON jp.owner_id = o.id " +
                     "WHERE ja.user_id = ? " +
                     "ORDER BY ja.apply_date DESC " +
                     "LIMIT ? OFFSET ?";
        
        int offset = (page - 1) * pageSize;
        return jdbcTemplate.queryForList(sql, userId, pageSize, offset);
    }

    // Get user applications count - FIXED
    public long getUserApplicationsCount(int userId) {
        String sql = "SELECT COUNT(*) FROM job_application WHERE user_id = ?";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    // Get user applications by status - NEW 22-10
    public List<Map<String, Object>> getUserApplicationsByStatus(int userId, String status) {
        String sql = "SELECT ja.*, jp.title as job_title, jp.job_type, jp.location, o.company_name, o.profile_photo as logo " +
                     "FROM job_application ja " +
                     "JOIN job_post jp ON ja.job_id = jp.id " +
                     "JOIN owner o ON jp.owner_id = o.id " +
                     "WHERE ja.user_id = ? AND ja.status = ? " +
                     "ORDER BY ja.apply_date DESC";
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", rs.getInt("id"));
            result.put("user_id", rs.getInt("user_id"));
            result.put("job_id", rs.getInt("job_id"));
            result.put("apply_date", rs.getTimestamp("apply_date"));
            result.put("status", rs.getString("status"));
            result.put("job_title", rs.getString("job_title"));
            result.put("job_type", rs.getString("job_type"));
            result.put("location", rs.getString("location"));
            result.put("company_name", rs.getString("company_name"));
            result.put("logo", rs.getBytes("logo"));
            
            byte[] cv = rs.getBytes("cv");
            result.put("cv", cv);
            result.put("has_cv", cv != null);
            
            return result;
        }, userId, status);
    }

    // Get user applications paginated by status - NEW
    public List<Map<String, Object>> getUserApplicationsPaginatedByStatus(int userId, String status, int page, int pageSize) {
        String sql = "SELECT ja.id, ja.apply_date, ja.status, ja.cv, " +
                     "jp.title as job_title, jp.job_type, jp.location, " +
                     "o.company_name " +
                     "FROM job_application ja " +
                     "JOIN job_post jp ON ja.job_id = jp.id " +
                     "JOIN owner o ON jp.owner_id = o.id " +
                     "WHERE ja.user_id = ? AND ja.status = ? " +
                     "ORDER BY ja.apply_date DESC " +
                     "LIMIT ? OFFSET ?";
        
        int offset = (page - 1) * pageSize;
        return jdbcTemplate.queryForList(sql, userId, status, pageSize, offset);
    }

    // Get user applications count by status - NEW //22-10
    public long getUserApplicationsCountByStatus(int userId, String status) {
        String sql = "SELECT COUNT(*) FROM job_application WHERE user_id = ? AND status = ?";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, userId, status);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
 // Count all job applications
    public long countAllApplications() {
        String sql = "SELECT COUNT(*) FROM job_application";
        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return count != null ? count : 0L;
        } catch (Exception e) {
            System.err.println("Error in countAllApplications: " + e.getMessage());
            return 0L;
        }
    }

 // Add this method to your JobApplicationRepository class
    public int countByUserIdAndJobId(int userId, int jobId) {
        try {
            String sql = "SELECT COUNT(*) FROM job_application WHERE user_id = ? AND job_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, jobId);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error in countByUserIdAndJobId: " + e.getMessage());
            return 0;
        }
    }
    
}
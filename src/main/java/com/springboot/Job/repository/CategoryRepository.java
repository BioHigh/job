package com.springboot.Job.repository;

import com.springboot.Job.model.CategoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CategoryRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class CategoryRowMapper implements RowMapper<CategoryBean> {
        @Override
        public CategoryBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            CategoryBean category = new CategoryBean();
            category.setId(rs.getInt("id"));
            category.setCatName(rs.getString("cat_name"));
            category.setAdminId(rs.getInt("admin_id"));
            return category;
        }
    }

    // ✅ Get job counts per category (only categories that have jobs)
    public Map<Integer, Integer> getJobCountsPerCategory() {
        String sql = "SELECT c.id, COUNT(j.id) as job_count " +
                    "FROM category c " +
                    "LEFT JOIN job_post j ON c.id = j.category_id " +
                    "WHERE j.status = 'APPROVED' " + // Only count approved jobs
                    "GROUP BY c.id " +
                    "HAVING COUNT(j.id) > 0";
        
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<Integer, Integer> jobCounts = new HashMap<>();
            while (rs.next()) {
                jobCounts.put(rs.getInt("id"), rs.getInt("job_count"));
            }
            return jobCounts;
        });
    }

    // ✅ Get all categories with job counts (including categories with 0 jobs)
    public Map<Integer, Integer> getAllCategoriesWithJobCounts() {
        String sql = "SELECT c.id, COUNT(j.id) as job_count " +
                    "FROM category c " +
                    "LEFT JOIN job_post j ON c.id = j.category_id AND j.status = 'APPROVED' " +
                    "GROUP BY c.id, c.cat_name " +
                    "ORDER BY job_count DESC";
        
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<Integer, Integer> jobCounts = new HashMap<>();
            while (rs.next()) {
                jobCounts.put(rs.getInt("id"), rs.getInt("job_count"));
            }
            return jobCounts;
        });
    }

    // ✅ Get categories that have at least one job
    public List<CategoryBean> findCategoriesWithJobs() {
        String sql = "SELECT DISTINCT c.* " +
                    "FROM category c " +
                    "INNER JOIN job_post j ON c.id = j.category_id " +
                    "WHERE j.status = 'APPROVED' " +
                    "ORDER BY c.cat_name";
        
        return jdbcTemplate.query(sql, new CategoryRowMapper());
    }

    // ✅ Get categories with job counts as a custom object
    public List<CategoryWithCount> getCategoriesWithJobCounts() {
        String sql = "SELECT c.id, c.cat_name, c.admin_id, COUNT(j.id) as job_count " +
                    "FROM category c " +
                    "LEFT JOIN job_post j ON c.id = j.category_id AND j.status = 'APPROVED' " +
                    "GROUP BY c.id, c.cat_name, c.admin_id " +
                    "HAVING COUNT(j.id) > 0 " +
                    "ORDER BY job_count DESC";
        
        return jdbcTemplate.query(sql, new RowMapper<CategoryWithCount>() {
            @Override
            public CategoryWithCount mapRow(ResultSet rs, int rowNum) throws SQLException {
                CategoryWithCount categoryWithCount = new CategoryWithCount();
                categoryWithCount.setId(rs.getInt("id"));
                categoryWithCount.setCatName(rs.getString("cat_name"));
                categoryWithCount.setAdminId(rs.getInt("admin_id"));
                categoryWithCount.setJobCount(rs.getInt("job_count"));
                return categoryWithCount;
            }
        });
    }

    // ✅ Create (Insert)
    public boolean save(CategoryBean category) {
        try {
            System.out.println("DEBUG: Repository save - Category: " + category.getCatName() + 
                             ", Admin ID: " + category.getAdminId());
            
            String sql = "INSERT INTO category (cat_name, admin_id) VALUES (?, ?)";
            int result = jdbcTemplate.update(sql, category.getCatName(), category.getAdminId());
            
            System.out.println("DEBUG: Repository save result: " + result);
            return result > 0;
        } catch (Exception e) {
            System.out.println("DEBUG: Repository save error: " + e.getMessage());
            throw e;
        }
    }

    // ✅ Find All
    public List<CategoryBean> findAll() {
        String sql = "SELECT * FROM category ORDER BY cat_name";
        return jdbcTemplate.query(sql, new CategoryRowMapper());
    }

    // ✅ Find by ID
    public Optional<CategoryBean> findById(int id) {
        try {
            String sql = "SELECT * FROM category WHERE id = ?";
            CategoryBean category = jdbcTemplate.queryForObject(sql, new CategoryRowMapper(), id);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // ✅ Update
    public boolean update(CategoryBean category) {
        try {
            System.out.println("DEBUG: Repository update - ID: " + category.getId() + 
                             ", Category: " + category.getCatName() + 
                             ", Admin ID: " + category.getAdminId());
            
            String sql = "UPDATE category SET cat_name = ?, admin_id = ? WHERE id = ?";
            int result = jdbcTemplate.update(sql, category.getCatName(), category.getAdminId(), category.getId());
            
            System.out.println("DEBUG: Repository update result: " + result);
            return result > 0;
        } catch (Exception e) {
            System.out.println("DEBUG: Repository update error: " + e.getMessage());
            throw e;
        }
    }

    // ✅ Delete
    public boolean delete(int id) {
        String sql = "DELETE FROM category WHERE id = ?";
        int result = jdbcTemplate.update(sql, id);
        return result > 0;
    }

    // Custom class to hold category with job count
    public static class CategoryWithCount {
        private int id;
        private String catName;
        private int adminId;
        private int jobCount;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getCatName() { return catName; }
        public void setCatName(String catName) { this.catName = catName; }
        
        public int getAdminId() { return adminId; }
        public void setAdminId(int adminId) { this.adminId = adminId; }
        
        public int getJobCount() { return jobCount; }
        public void setJobCount(int jobCount) { this.jobCount = jobCount; }
    }
}
package com.springboot.Job.repository;

import com.springboot.Job.model.CategoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
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
        String sql = "SELECT * FROM category";
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
}

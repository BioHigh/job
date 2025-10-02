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
            // still map admin_id if you want to display it later
            category.setAdminId(rs.getInt("admin_id"));
            return category;
        }
    }

    // ✅ Create (Insert) — always link to admin_id = 1
    public boolean save(CategoryBean category) {
        String sql = "INSERT INTO category (cat_name, admin_id) VALUES (?, ?)";
        int result = jdbcTemplate.update(sql, category.getCatName(), 1);
        return result > 0;
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

    // ✅ Update — always keep admin_id = 1
    public boolean update(CategoryBean category) {
        String sql = "UPDATE category SET cat_name = ?, admin_id = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, category.getCatName(), 1, category.getId());
        return result > 0;
    }

    // ✅ Delete
    public boolean delete(int id) {
        String sql = "DELETE FROM category WHERE id = ?";
        int result = jdbcTemplate.update(sql, id);
        return result > 0;
    }
}

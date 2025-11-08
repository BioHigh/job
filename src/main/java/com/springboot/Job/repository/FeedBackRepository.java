package com.springboot.Job.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.FeedBackBean;

@Repository
public class FeedBackRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public int saveFeedback(FeedBackBean feedback) {
        String sql = "INSERT INTO feedback (fbmessage, user_id, owner_id) VALUES (?, ?, ?)";
        
        if (feedback.getUser_id() != null && feedback.getOwner_id() != null) {
            throw new IllegalArgumentException("Cannot provide both user_id and owner_id");
        }
        
        if (feedback.getUser_id() == null && feedback.getOwner_id() == null) {
            throw new IllegalArgumentException("Either user_id or owner_id must be provided");
        }
        
        return jdbcTemplate.update(sql,
                feedback.getFbmessage(),
                feedback.getUser_id(),
                feedback.getOwner_id());
    }
    
    // Get feedback with pagination
    public List<FeedBackBean> findWithPagination(int offset, int size) {
        String sql = "SELECT * FROM feedback ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new Object[]{size, offset}, (rs, rowNum) -> {
            FeedBackBean fb = new FeedBackBean();
            fb.setId(rs.getInt("id"));
            fb.setFbmessage(rs.getString("fbmessage"));
            fb.setCreated_at(rs.getTimestamp("created_at"));
            fb.setUser_id(rs.getInt("user_id"));
            fb.setOwner_id(rs.getInt("owner_id"));
            return fb;
        });
    }
    
    // Count total feedback for pagination
    public int countAllFeedbacks() {
        String sql = "SELECT COUNT(*) FROM feedback";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
    
    // Keep your existing method for backward compatibility
    public List<FeedBackBean> getAllFeedbacks() {
        String sql = "SELECT * FROM feedback ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            FeedBackBean fb = new FeedBackBean();
            fb.setId(rs.getInt("id"));
            fb.setFbmessage(rs.getString("fbmessage"));
            fb.setCreated_at(rs.getTimestamp("created_at"));
            fb.setUser_id(rs.getInt("user_id"));
            fb.setOwner_id(rs.getInt("owner_id"));
            return fb;
        });
    }
}
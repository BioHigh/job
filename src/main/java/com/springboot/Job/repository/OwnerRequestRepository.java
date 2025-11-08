package com.springboot.Job.repository;

import com.springboot.Job.model.OwnerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OwnerRequestRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class OwnerRequestRowMapper implements RowMapper<OwnerRequest> {
        @Override
        public OwnerRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            OwnerRequest request = new OwnerRequest();
            request.setId(rs.getInt("id"));
            request.setCompanyName(rs.getString("company_name"));
            request.setGmail(rs.getString("gmail"));
            request.setCompanyPhone(rs.getString("company_phone"));
            request.setDescription(rs.getString("description"));
            request.setCity(rs.getString("address"));
            request.setTownship(rs.getString("township"));
            request.setStatus(rs.getString("status"));
            request.setAuthKey(rs.getString("auth_key"));
            
            java.sql.Timestamp requestDate = rs.getTimestamp("request_date");
            if (requestDate != null) {
                request.setRequestDate(requestDate.toLocalDateTime());
            }
            
            request.setAdminId(rs.getInt("admin_id"));
            request.setOwnerId(rs.getInt("owner_id"));
            
            return request;
        }
    }

    public boolean createRequest(OwnerRequest request) {
        String sql = "INSERT INTO owner_request (company_name, gmail, company_phone, description, address, township, auth_key, request_date) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
            request.getCompanyName(),
            request.getGmail(),
            request.getCompanyPhone(),
            request.getDescription(),
            request.getCity(),
            request.getTownship(),
            request.getAuthKey()
        );
        return result > 0;
    }

    public Optional<OwnerRequest> findById(int id) {
        try {
            String sql = "SELECT * FROM owner_request WHERE id = ?";
            OwnerRequest request = jdbcTemplate.queryForObject(sql, new OwnerRequestRowMapper(), id);
            return Optional.ofNullable(request);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<OwnerRequest> findByAuthKey(String authKey) {
        try {
            String sql = "SELECT * FROM owner_request WHERE auth_key = ?";
            OwnerRequest request = jdbcTemplate.queryForObject(sql, new OwnerRequestRowMapper(), authKey);
            return Optional.ofNullable(request);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<OwnerRequest> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM owner_request WHERE gmail = ?";
            OwnerRequest request = jdbcTemplate.queryForObject(sql, new OwnerRequestRowMapper(), email);
            return Optional.ofNullable(request);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<OwnerRequest> findPendingRequests() {
        String sql = "SELECT * FROM owner_request WHERE status = 'PENDING' ORDER BY request_date";
        return jdbcTemplate.query(sql, new OwnerRequestRowMapper());
    }

    public List<OwnerRequest> findPendingRequestsWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM owner_request WHERE status = 'PENDING' ORDER BY request_date LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new OwnerRequestRowMapper(), limit, offset);
    }

    public int countPendingRequests() {
        String sql = "SELECT COUNT(*) FROM owner_request WHERE status = 'PENDING'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<OwnerRequest> findAllRequests() {
        String sql = "SELECT * FROM owner_request ORDER BY request_date DESC";
        return jdbcTemplate.query(sql, new OwnerRequestRowMapper());
    }

    public List<OwnerRequest> findAllRequestsWithPagination(int offset, int limit) {
        String sql = "SELECT * FROM owner_request ORDER BY request_date DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new OwnerRequestRowMapper(), limit, offset);
    }

    public int countAllRequests() {
        String sql = "SELECT COUNT(*) FROM owner_request";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public boolean approveRequest(int requestId, int adminId) {
        String sql = "UPDATE owner_request SET status = 'APPROVED', admin_id = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, adminId, requestId);
        return result > 0;
    }

    public boolean rejectRequest(int requestId, int adminId) {
        String sql = "UPDATE owner_request SET status = 'REJECTED', admin_id = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, adminId, requestId);
        return result > 0;
    }

    public boolean linkOwnerToRequest(int requestId, int ownerId) {
        String sql = "UPDATE owner_request SET owner_id = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, ownerId, requestId);
        return result > 0;
    }
    
    public Optional<OwnerRequest> findByPhone(String phone) {
        try {
            String sql = "SELECT * FROM owner_request WHERE company_phone = ? AND status = 'PENDING'";
            OwnerRequest request = jdbcTemplate.queryForObject(sql, new OwnerRequestRowMapper(), phone);
            return Optional.ofNullable(request);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public List<OwnerRequest> findRequestsByStatusWithPagination(String status, int offset, int limit) {
        String sql = "SELECT * FROM owner_request WHERE status = ? ORDER BY request_date DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new OwnerRequestRowMapper(), status, limit, offset);
    }

    public int countRequestsByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM owner_request WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, status);
    }
    
}
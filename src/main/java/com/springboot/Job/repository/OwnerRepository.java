package com.springboot.Job.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.Owner;

@Repository
public class OwnerRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class OwnerRowMapper implements RowMapper<Owner> {
        @Override
        public Owner mapRow(ResultSet rs, int rowNum) throws SQLException {
            Owner owner = new Owner();
            owner.setId(rs.getInt("id"));
            owner.setCompanyName(rs.getString("company_name"));
            owner.setPassword(rs.getString("password"));
            owner.setGmail(rs.getString("gmail"));
            owner.setCompanyPhone(rs.getString("company_phone"));
            owner.setDescription(rs.getString("description"));
            owner.setAddress(rs.getString("address"));
            owner.setTownship(rs.getString("township"));
            owner.setStatus(rs.getString("status"));
            owner.setType(rs.getString("type"));
            owner.setAuthKey(rs.getString("auth_key"));
            
            // Handle profile_photo
            byte[] profilePhoto = rs.getBytes("profile_photo");
            owner.setProfilePhoto(profilePhoto);
            
            // Handle created_at
            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                owner.setCreatedAt(createdAt.toLocalDateTime());
            }
            
            return owner;
        }
    }

    public Optional<Owner> findByEmailAndPassword(String email, String password) {
        try {
            String sql = "SELECT * FROM owner WHERE gmail = ? AND password = ? AND status = 'active'";
            Owner owner = jdbcTemplate.queryForObject(sql, new OwnerRowMapper(), email, password);
            return Optional.ofNullable(owner);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Owner> findById(int id) {
        try {
            String sql = "SELECT * FROM owner WHERE id = ?";
            Owner owner = jdbcTemplate.queryForObject(sql, new OwnerRowMapper(), id);
            return Optional.ofNullable(owner);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Owner> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM owner WHERE gmail = ?";
            Owner owner = jdbcTemplate.queryForObject(sql, new OwnerRowMapper(), email);
            return Optional.ofNullable(owner);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Owner> findByPhone(String phone) {
        try {
            String sql = "SELECT * FROM owner WHERE company_phone = ?";
            Owner owner = jdbcTemplate.queryForObject(sql, new OwnerRowMapper(), phone);
            return Optional.ofNullable(owner);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean updateOwner(Owner owner, byte[] profilePhoto) {
        String sql = "UPDATE owner SET company_name = ?, company_phone = ?, description = ?, address = ?, township = ?, profile_photo = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, 
            owner.getCompanyName(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            owner.getTownship(), // township
            profilePhoto,
            owner.getId()
        );
        return result > 0;
    }

    public boolean updateOwnerWithoutPhoto(Owner owner) {
        String sql = "UPDATE owner SET company_name = ?, company_phone = ?, description = ?, address = ?, township = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, 
            owner.getCompanyName(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            owner.getTownship(), // township
            owner.getId()
        );
        return result > 0;
    }

    public boolean createOwner(Owner owner, byte[] profilePhoto) {
        String sql = "INSERT INTO owner (company_name, password, gmail, company_phone, description, address, township, profile_photo, status, type, auth_key, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'active', 'COMPANY', ?, CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
            owner.getCompanyName(),
            owner.getPassword(),
            owner.getGmail(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            owner.getTownship(),
            profilePhoto,
            owner.getAuthKey()
        );
        return result > 0;
    }

    public boolean createOwnerWithoutPhoto(Owner owner) {
        String sql = "INSERT INTO owner (company_name, password, gmail, company_phone, description, address, township, status, type, auth_key, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 'active', 'COMPANY', ?, CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
            owner.getCompanyName(),
            owner.getPassword(),
            owner.getGmail(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            owner.getTownship(),
            owner.getAuthKey()
        );
        return result > 0;
    }

    public byte[] getProfilePhotoById(int ownerId) {
        try {
            String sql = "SELECT profile_photo FROM owner WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, byte[].class, ownerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    public List<Owner> findAllCompanies() {
        String sql = "SELECT * FROM owner WHERE status = 'active' ORDER BY company_name";
        return jdbcTemplate.query(sql, new OwnerRowMapper());
    }
    
    public boolean updateAuthKey(String email, String authKey) {
        String sql = "UPDATE owner SET auth_key = ? WHERE gmail = ?";
        int result = jdbcTemplate.update(sql, authKey, email);
        return result > 0;
    }
    
    public Optional<Owner> findByAuthKey(String authKey) {
        try {
            String sql = "SELECT * FROM owner WHERE auth_key = ?";
            Owner owner = jdbcTemplate.queryForObject(sql, new OwnerRowMapper(), authKey);
            return Optional.ofNullable(owner);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE owner SET password = ? WHERE gmail = ?";
        int result = jdbcTemplate.update(sql, newPassword, email);
        return result > 0;
    }
    
    //4-11-25 Pagination-update
    
    public List<Owner> findActiveCompaniesWithPagination(int limit, int offset) {
        String sql = "SELECT * FROM owner WHERE status = 'active' ORDER BY company_name LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, new OwnerRowMapper(), limit, offset);
    }

    public int countActiveCompanies() {
        String sql = "SELECT COUNT(*) FROM owner WHERE status = 'active'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
            
    }
 // Search methods - Updated to use non-deprecated methods
    public List<Owner> searchActiveCompanies(String search) {
        String sql = """
            SELECT * FROM owner 
            WHERE status = 'active' 
            AND (company_name LIKE ? OR description LIKE ? OR address LIKE ? OR gmail LIKE ? OR company_phone LIKE ? OR township LIKE ?)
            ORDER BY company_name
            """;
        
        String searchPattern = "%" + search + "%";
        return jdbcTemplate.query(sql, new OwnerRowMapper(), 
            searchPattern, searchPattern, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    public List<Owner> searchActiveCompaniesWithPagination(String search, int limit, int offset) {
        String sql = """
            SELECT * FROM owner 
            WHERE status = 'active' 
            AND (company_name LIKE ? OR description LIKE ? OR address LIKE ? OR gmail LIKE ? OR company_phone LIKE ? OR township LIKE ?)
            ORDER BY company_name 
            LIMIT ? OFFSET ?
            """;
        
        String searchPattern = "%" + search + "%";
        return jdbcTemplate.query(sql, new OwnerRowMapper(), 
            searchPattern, searchPattern, searchPattern, searchPattern, searchPattern, searchPattern, limit, offset);
    }
    
    public int countSearchActiveCompanies(String search) {
        String sql = """
            SELECT COUNT(*) FROM owner 
            WHERE status = 'active' 
            AND (company_name LIKE ? OR description LIKE ? OR address LIKE ? OR gmail LIKE ? OR company_phone LIKE ? OR township LIKE ?)
            """;
        
        String searchPattern = "%" + search + "%";
        return jdbcTemplate.queryForObject(sql, Integer.class, 
            searchPattern, searchPattern, searchPattern, searchPattern, searchPattern, searchPattern);
    }
}
    

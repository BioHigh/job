package com.springboot.Job.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
            owner.setStatus(rs.getString("status"));
            owner.setType(rs.getString("type"));
            
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
        String sql = "UPDATE owner SET company_name = ?, company_phone = ?, description = ?, address = ?, profile_photo = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, 
            owner.getCompanyName(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            profilePhoto,
            owner.getId()
        );
        return result > 0;
    }

    public boolean updateOwnerWithoutPhoto(Owner owner) {
        String sql = "UPDATE owner SET company_name = ?, company_phone = ?, description = ?, address = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, 
            owner.getCompanyName(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            owner.getId()
        );
        return result > 0;
    }

    public boolean createOwner(Owner owner, byte[] profilePhoto) {
        String sql = "INSERT INTO owner (company_name, password, gmail, company_phone, description, address, profile_photo, status, type, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, 'active', 'COMPANY', CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
            owner.getCompanyName(),
            owner.getPassword(),
            owner.getGmail(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress(),
            profilePhoto
        );
        return result > 0;
    }

    public boolean createOwnerWithoutPhoto(Owner owner) {
        String sql = "INSERT INTO owner (company_name, password, gmail, company_phone, description, address, status, type, created_at) VALUES (?, ?, ?, ?, ?, ?, 'active', 'COMPANY', CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
            owner.getCompanyName(),
            owner.getPassword(),
            owner.getGmail(),
            owner.getCompanyPhone(),
            owner.getDescription(),
            owner.getAddress()
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
}
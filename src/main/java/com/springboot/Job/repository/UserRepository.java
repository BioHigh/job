package com.springboot.Job.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.UserBean;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static class UserRowMapper implements RowMapper<UserBean> {
        @Override
        public UserBean mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserBean user = new UserBean();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            user.setDateOfBirth(rs.getString("date_of_birth"));
            user.setPassword(rs.getString("password"));
            user.setGmail(rs.getString("gmail"));
            user.setGender(rs.getString("gender"));
            user.setPhone(rs.getString("phone"));
            user.setLocation(rs.getString("location"));
            user.setUserType(rs.getString("user_type"));
            user.setStatus(rs.getString("status"));

            // Professional fields
            user.setProfession(rs.getString("profession"));
            user.setEducation(rs.getString("education"));
            user.setSkills(rs.getString("skills"));
            user.setExperienceYears(rs.getInt("experience_years"));

            // Handle created_at as LocalDateTime
            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }

            // Handle updated_at as LocalDateTime
            java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                user.setUpdatedAt(updatedAt.toLocalDateTime());
            }

            return user;
        }
    }

    // =================== FIND METHODS ===================
    public Optional<UserBean> findByEmailAndPassword(String email, String password) {
        try {
            String sql = "SELECT * FROM user WHERE gmail = ? AND password = ? AND status = 'active'";
            UserBean user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email, password);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UserBean> findById(int id) {
        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            UserBean user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UserBean> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM user WHERE gmail = ?";
            UserBean user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UserBean> findByPhone(String phone) {
        try {
            String sql = "SELECT * FROM user WHERE phone = ?";
            UserBean user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), phone);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // =================== CREATE/UPDATE METHODS ===================
    public boolean createUser(UserBean user, byte[] profilePhoto) {
        String sql = "INSERT INTO user (name, age, date_of_birth, password, gmail, gender, phone, profile_photo, user_type, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'user', 'active', CURRENT_TIMESTAMP)";
        int result = jdbcTemplate.update(sql,
                user.getName(),
                user.getAge(),
                user.getDateOfBirth(),
                user.getPassword(),
                user.getGmail(),
                user.getGender(),
                user.getPhone(),
                profilePhoto
        );
        return result > 0;
    }

    public boolean updateUser(UserBean user, byte[] profilePhoto) {
        String sql = "UPDATE user SET name = ?, age = ?, date_of_birth = ?, gender = ?, phone = ?, profile_photo = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql,
                user.getName(),
                user.getAge(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhone(),
                profilePhoto,
                user.getId()
        );
        return result > 0;
    }

    public boolean updateUserWithoutPhoto(UserBean user) {
        String sql = "UPDATE user SET name = ?, age = ?, date_of_birth = ?, gender = ?, phone = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql,
                user.getName(),
                user.getAge(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getPhone(),
                user.getId()
        );
        return result > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE user SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE gmail = ?";
        int result = jdbcTemplate.update(sql, newPassword, email);
        return result > 0;
    }

    // =================== PROFESSIONAL PROFILE METHODS ===================
    public boolean updateProfessionalProfile(UserBean user) {
        String sql = "UPDATE user SET profession = ?, experience_years = ?, education = ?, location = ?, skills = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        int result = jdbcTemplate.update(sql,
                user.getProfession(),
                user.getExperienceYears(),
                user.getEducation(),
                user.getLocation(),
                user.getSkills(),
                user.getId()
        );
        return result > 0;
    }

    public boolean updateProfession(int userId, String profession) {
        String sql = "UPDATE user SET profession = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, profession, userId);
        return result > 0;
    }

    public boolean updateExperience(int userId, Integer experienceYears) {
        String sql = "UPDATE user SET experience_years = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, experienceYears, userId);
        return result > 0;
    }

    public boolean updateEducation(int userId, String education) {
        String sql = "UPDATE user SET education = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, education, userId);
        return result > 0;
    }

    public boolean updateLocation(int userId, String location) {
        String sql = "UPDATE user SET location = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, location, userId);
        return result > 0;
    }

    public boolean updateSkills(int userId, String skills) {
        String sql = "UPDATE user SET skills = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, skills, userId);
        return result > 0;
    }

    // =================== PROFILE PHOTO ===================
    public byte[] getProfilePhotoById(int userId) {
        try {
            String sql = "SELECT profile_photo FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, byte[].class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // =================== RESUME METHODS ===================
    public boolean updateResume(int userId, byte[] resume) {
        String sql = "UPDATE user SET resume = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = jdbcTemplate.update(sql, resume, userId);
        return result > 0;
    }

    public byte[] getResumeById(int userId) {
        try {
            String sql = "SELECT resume FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, byte[].class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    
    //======= NEW METHOD =======
    public Optional<UserBean> findUserProfileById(int userId) {
        try {
            String sql = "SELECT * FROM user WHERE id = ?";
            UserBean user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), userId);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
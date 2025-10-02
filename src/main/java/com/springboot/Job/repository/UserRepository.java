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
            user.setUserType(rs.getString("user_type"));
            user.setStatus(rs.getString("status"));
            return user;
        }
    }

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

    public boolean updateUser(UserBean user, byte[] profilePhoto) {
        String sql = "UPDATE user SET name = ?, age = ?, date_of_birth = ?, gender = ?, phone = ?, profile_photo = ? WHERE id = ?";
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
        String sql = "UPDATE user SET name = ?, age = ?, date_of_birth = ?, gender = ?, phone = ? WHERE id = ?";
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

    public boolean createUser(UserBean user, byte[] profilePhoto) {
        String sql = "INSERT INTO user (name, age, date_of_birth, password, gmail, gender, phone, profile_photo, user_type, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'USER', 'ACTIVE')";
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

    public byte[] getProfilePhotoById(int userId) {
        try {
            String sql = "SELECT profile_photo FROM user WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, byte[].class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
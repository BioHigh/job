package com.springboot.Job.repository;

import com.springboot.Job.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LocationRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class LocationRowMapper implements RowMapper<Location> {
        @Override
        public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
            Location location = new Location();
            try {
                location.setId(rs.getInt("id"));
            } catch (SQLException e) {
                location.setId(null);
            }
            location.setCityName(rs.getString("city_name"));
            location.setType(rs.getString("type"));
            return location;
        }
    }

    public List<Location> findAll() {
        String sql = "SELECT * FROM location ORDER BY city_name";
        return jdbcTemplate.query(sql, new LocationRowMapper());
    }

    public Optional<Location> findById(Integer id) {
        try {
            String sql = "SELECT * FROM location WHERE id = ?";
            Location location = jdbcTemplate.queryForObject(sql, new LocationRowMapper(), id);
            return Optional.ofNullable(location);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Location> findByCityName(String cityName) {
        try {
            String sql = "SELECT * FROM location WHERE city_name = ?";
            Location location = jdbcTemplate.queryForObject(sql, new LocationRowMapper(), cityName);
            return Optional.ofNullable(location);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
package com.springboot.Job.repository;

import com.springboot.Job.model.Township;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class TownshipRepository {

    private final JdbcTemplate jdbcTemplate;

    private static class TownshipRowMapper implements RowMapper<Township> {
        @Override
        public Township mapRow(ResultSet rs, int rowNum) throws SQLException {
            Township t = new Township();
            t.setId(rs.getInt("id"));
            t.setTownshipName(rs.getString("township_name"));
            t.setCityId(rs.getInt("city_id"));
            try {
                t.setCityName(rs.getString("city_name"));
            } catch (SQLException e) {
                t.setCityName(null);
            }
            return t;
        }
    }

    public List<Township> findAllWithCityNames() {
        String sql = "SELECT t.*, l.city_name FROM township t " +
                     "LEFT JOIN location l ON t.city_id = l.id " +
                     "ORDER BY l.city_name, t.township_name";
        return jdbcTemplate.query(sql, new TownshipRowMapper());
    }
}

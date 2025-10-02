package com.springboot.Job.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.springboot.Job.model.AdminLoginBean;
import com.springboot.Job.model.LoginBean;

@Repository
public class AdminLoginRepository {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public List<AdminLoginBean> loginAdmin(LoginBean obj) {
		String sql = "select * from admin where name = ?  and gmail= ?  and password = ?";
		List<AdminLoginBean> admin = jdbcTemplate.query(
				sql,
				(rs,rowNumber) ->
				new AdminLoginBean(
					rs.getInt("id"),
					rs.getString("name"),
					rs.getString("gmail"),
					rs.getString("password")),
				obj.getName(),obj.getGmail(),obj.getPassword());
					return admin;
				
		
	}
}


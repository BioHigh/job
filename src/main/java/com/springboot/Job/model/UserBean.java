package com.springboot.Job.model;


import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBean {
    
    private Integer id; 
    private String name;
    private Integer age; 
    private String dateOfBirth; 
    private String password;
    private String gmail;
    private String gender;
    private String phone;
    private String userType;
    private String status;
    private LocalDateTime createdAt;
}
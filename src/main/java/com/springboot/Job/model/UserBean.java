package com.springboot.Job.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Transient;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBean {
    
    private Integer id; 
    private String name;
    private String dateOfBirth; 
    private Integer age; 
    private String password;
    private String gmail;
    private String gender;
    private String phone;
    private String location;
    
    @Transient
    private byte[] profilePhoto; // Changed from String to byte[] for longblob
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status; // enum('active','inactive')
    private String userType; // enum('user')
    
    // Professional fields from your table
    private String profession;
    private String education;
    private String skills; // text type
    private Integer experienceYears;
    private byte[] resume; // longblob type
    
    // Constructors
    public UserBean() {}
    
    public UserBean(Integer id, String name, String gmail) {
        this.id = id;
        this.name = name;
        this.gmail = gmail;
    }
}
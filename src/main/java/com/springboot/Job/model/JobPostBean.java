package com.springboot.Job.model;

import lombok.Getter;

import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class JobPostBean {
    private Integer id;
    private Integer categoryId;
    private String jobTitle;
    private String jobType;
    private String department;
    private String location;
    private String jobDescription;
    private String companyName;
    private String companyWebsite;
    private String companyDescription;
    private String requiredSkills;
    private String experienceLevel;
    private String educationLevel;
    private Integer salaryMini;
    private Integer salaryMax;
    private String benefits;
    private String applicationEmail;
    private String applicationDeadline;
    private String applicationInstructions;
    private Integer ownerId;
    private Integer adminId;
    private String status;
    private LocalDateTime createdAt;
    
}
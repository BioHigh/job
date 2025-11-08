package com.springboot.Job.model;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobApplicationBean {
    
    private int id;
    private int userId;
    private int jobId;
    private byte[] cvFile; 
    private Timestamp applyDate;
    private String status;
    private Boolean isSeen; // ADD THIS FIELD
    
    private String userName;
    private String userEmail;
    private String userPhone;
    private String jobTitle;
    private String companyName;
    
    public boolean hasCv() {
        return cvFile != null && cvFile.length > 0;
    }
}
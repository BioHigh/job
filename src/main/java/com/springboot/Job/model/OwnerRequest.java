package com.springboot.Job.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OwnerRequest {
    private Integer id;
    private String companyName;
    private String gmail;
    private String companyPhone;
    private String description;
    private String city;
    private String township;
    private String status = "PENDING";
    private String authKey;
    private LocalDateTime requestDate;
    private Integer adminId;
    private Integer ownerId;

    public boolean hasCompleteData() {
        return companyName != null && !companyName.trim().isEmpty()
            && gmail != null && !gmail.trim().isEmpty();
    }
}

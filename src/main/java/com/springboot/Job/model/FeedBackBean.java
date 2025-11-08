package com.springboot.Job.model;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedBackBean {
    private int id;
    private String fbmessage;
    private Timestamp created_at;
    private Integer user_id;
    private Integer owner_id;
}
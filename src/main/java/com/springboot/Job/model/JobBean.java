package com.springboot.Job.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "job_post")
public class JobBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "owner_id")
    private int ownerId;

    @Column(name = "category_id")
    private int categoryId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "post_date")
    private LocalDateTime postDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "admin_id")
    private Integer adminId;

    // enum for status
    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }
}

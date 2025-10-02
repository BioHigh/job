package com.springboot.Job.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.springboot.Job.model.JobBean;

@Repository
public interface JobRepository extends JpaRepository<JobBean, Integer> {
    // extra queries can be added later if needed
}

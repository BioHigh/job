package com.springboot.Job.model;

import lombok.Data;

@Data
public class Township {
    private Integer id;
    private String townshipName;
    private Integer cityId;
    private String cityName;
}

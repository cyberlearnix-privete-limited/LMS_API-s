package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetails {
    private String courseId;
    private String courseName;
    private String instructorId;
    private Double price;
}
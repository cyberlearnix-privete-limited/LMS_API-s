package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequest {
    private String courseId;
    private String instructorId;

    // These replace the "Course Service" lookup
    private String courseName;
    private Double price;
}
package com.lms.coupon_service.dto;

import lombok.Data;

@Data
public class ValidateRequest {
    private String couponCode;
    private String userId;
    private String courseId;
    private Double price;
}
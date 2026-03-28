package com.lms.coupon_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponRequest {
    private String couponCode;
    private String userId;
    private String courseId;
    private Double price;
}
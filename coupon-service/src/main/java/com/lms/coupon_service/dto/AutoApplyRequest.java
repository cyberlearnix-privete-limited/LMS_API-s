package com.lms.coupon_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoApplyRequest {
    private String userId;
    private String courseId;
    private Double price;
}
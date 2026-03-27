package com.lms.coupon_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponResponse {
    private boolean valid;
    private Double discount;
    private Double finalPrice;
    private String message;
}
package com.lms.coupon_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResponse {
    private boolean valid;
    private Double discount;
    private Double finalPrice;
    private String message;
}
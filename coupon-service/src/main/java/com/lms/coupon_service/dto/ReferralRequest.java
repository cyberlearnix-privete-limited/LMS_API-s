package com.lms.coupon_service.dto;

import lombok.Data;

@Data
public class ReferralRequest {
    private String referrerId;
    private String referredUserId; // Optional: target user
}
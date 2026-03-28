package com.lms.coupon_service.dto;

import lombok.*;
import java.time.LocalDateTime; // Add this line

@Data
public class BulkRequest {
    private String campaignName;
    private String discountType;
    private Double discountValue;
    private Integer totalCoupons;
    private LocalDateTime startTime; // NEW: Start of the campaign
    private LocalDateTime expiryDate; // This acts as your endTime
    private String creatorRole;
    private String courseId;
}
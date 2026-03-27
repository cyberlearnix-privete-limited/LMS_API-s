package com.lms.coupon_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BulkResponse {
    private String batchId;
    private int totalGenerated;
    private String campaignName;
}
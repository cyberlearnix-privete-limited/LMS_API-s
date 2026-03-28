package com.lms.coupon_service.service;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import java.util.List;
import java.util.Map;

public interface CouponService {
    Map<String, Object> createCoupon(Coupon coupon);
    Map<String, Object> createCouponFromMap(Map<String, Object> couponData);
    List<Coupon> findAll();
    Coupon findByCode(String code);
    void deleteById(String id);

    ValidationResponse validateCoupon(ValidateRequest request);
    Map<String, Object> redeemCoupon(RedeemRequest request);

    // UPDATED: Use ValidationResponse instead of Map
    ValidationResponse autoApply(AutoApplyRequest request);

    // UPDATED: Use BulkResponse instead of Map
    BulkResponse bulkGenerate(BulkRequest request);

    List<Coupon> getCouponsByCampaignId(String campaignId);
    List<Coupon> getCouponsByUserId(String userId);
    Map<String, Object> generateReferralCoupon(String referrerId);
    Map<String, Object> assignCouponToUser(String code, String userId);
}
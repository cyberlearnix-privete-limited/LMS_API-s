package com.lms.coupon_service.repository;

import com.lms.coupon_service.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, String> {

    // PRD Section 13: Count usage for per-user limit enforcement
    long countByUserIdAndCouponCode(String userId, String couponCode);
}
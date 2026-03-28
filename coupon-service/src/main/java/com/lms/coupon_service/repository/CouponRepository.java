package com.lms.coupon_service.repository;

import com.lms.coupon_service.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {

    /**
     * Finds a coupon by its unique code (e.g., "AI50").
     * PRD Section 14: This should be indexed in the Entity for < 100ms latency.
     */
    Optional<Coupon> findByCode(String code);

    /**
     * PRD Section 9: Retrieve all coupons assigned to a specific user.
     */
    List<Coupon> findByAssignedUserId(String assignedUserId);

    /**
     * PRD Section 10: Retrieve all coupons belonging to a specific marketing campaign.
     */
    List<Coupon> findByCampaignId(String campaignId);

    /**
     * Optimized check to see if a code exists without loading the entire entity.
     */
    boolean existsByCode(String code);
}
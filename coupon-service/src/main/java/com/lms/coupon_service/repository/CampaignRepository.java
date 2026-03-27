package com.lms.coupon_service.repository;

import com.lms.coupon_service.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    Optional<Campaign> findByName(String name);
}
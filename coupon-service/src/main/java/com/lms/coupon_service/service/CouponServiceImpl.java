package com.lms.coupon_service.service;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import com.lms.coupon_service.entity.CouponUsage;
import com.lms.coupon_service.repository.CouponRepository;
import com.lms.coupon_service.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository repository;
    private final CouponUsageRepository usageRepository;

    @Override
    public ValidationResponse validateCoupon(ValidateRequest request) {
        Coupon coupon = findByCode(request.getCouponCode());
        LocalDateTime now = LocalDateTime.now();

        if (!coupon.isActive()) return buildValidationError("Coupon is inactive");

        if (coupon.getStartTime() != null && coupon.getStartTime().isAfter(now)) {
            return buildValidationError("Coupon not active yet. Starts at: " + coupon.getStartTime());
        }

        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
            return buildValidationError("Coupon has expired.");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            return buildValidationError("Maximum global usage limit reached.");
        }

        if (coupon.getPerUserLimit() != null) {
            long userUsageCount = usageRepository.countByUserIdAndCouponCode(request.getUserId(), coupon.getCode());
            if (userUsageCount >= coupon.getPerUserLimit()) {
                return buildValidationError("Personal limit reached.");
            }
        }

        if (coupon.getCourses() != null && !coupon.getCourses().isEmpty()) {
            if (!coupon.getCourses().contains(request.getCourseId())) {
                return buildValidationError("Not valid for this course.");
            }
        }

        double discount = calculateDiscountAmount(coupon, request.getPrice());

        return ValidationResponse.builder()
                .valid(true)
                .discount(discount)
                .finalPrice(request.getPrice() - discount)
                .message("Valid")
                .build();
    }

    @Override
    @Transactional
    public ValidationResponse autoApply(AutoApplyRequest r) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> eligible = repository.findAll().stream()
                .filter(c -> c.isActive())
                .filter(c -> (c.getStartTime() == null || c.getStartTime().isBefore(now)))
                .filter(c -> (c.getEndTime() == null || c.getEndTime().isAfter(now)))
                .filter(c -> c.getCourses() == null || c.getCourses().isEmpty() || c.getCourses().contains(r.getCourseId()))
                .filter(c -> c.getAssignedUserId() == null || c.getAssignedUserId().equals(r.getUserId()))
                .collect(Collectors.toList());

        if (eligible.isEmpty()) return buildValidationError("No applicable coupons found.");

        Coupon best = eligible.stream()
                .max(Comparator.comparingDouble(c -> calculateDiscountAmount(c, r.getPrice())))
                .orElse(null);

        double discount = calculateDiscountAmount(best, r.getPrice());

        return ValidationResponse.builder()
                .valid(true)
                .discount(discount)
                .finalPrice(r.getPrice() - discount)
                .message("Applied: " + best.getCode())
                .build();
    }

    @Override
    @Transactional
    public BulkResponse bulkGenerate(BulkRequest r) {
        String batchId = "BATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("INSTRUCTOR".equalsIgnoreCase(r.getCreatorRole()) && (r.getCourseId() == null || r.getCourseId().isEmpty())) {
            throw new RuntimeException("Instructors must provide a Course ID for bulk generation.");
        }

        for (int i = 0; i < r.getTotalCoupons(); i++) {
            String code = r.getCampaignName().toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

            repository.save(Coupon.builder()
                    .code(code)
                    .campaignId(r.getCampaignName())
                    .discountType(r.getDiscountType())
                    .discountValue(r.getDiscountValue())
                    .startTime(r.getStartTime() != null ? r.getStartTime() : LocalDateTime.now())
                    .endTime(r.getExpiryDate())
                    .courses(r.getCourseId() != null ? List.of(r.getCourseId()) : null)
                    .isActive(true)
                    .usedCount(0)
                    .build());
        }

        return BulkResponse.builder()
                .batchId(batchId)
                .totalGenerated(r.getTotalCoupons())
                .campaignName(r.getCampaignName())
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> redeemCoupon(RedeemRequest request) {
        Coupon coupon = findByCode(request.getCouponCode());

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(now)) {
            throw new RuntimeException("Coupon expired.");
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        repository.save(coupon);

        usageRepository.save(CouponUsage.builder()
                .userId(request.getUserId())
                .couponCode(coupon.getCode())
                .courseId(request.getCourseId())
                .usedAt(LocalDateTime.now())
                .build());

        return Map.of("success", true, "message", "Redeemed successfully");
    }

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public void deleteById(String id) {
        // Check if it exists. If not, throw an exception that the Global Handler will catch.
        if (!repository.existsById(id)) {
            throw new RuntimeException("Coupon not found or already deleted");
        }
        repository.deleteById(id);
    }

    private double calculateDiscountAmount(Coupon c, double price) {
        if (c == null) return 0.0;
        if ("PERCENT".equalsIgnoreCase(c.getDiscountType())) return (price * c.getDiscountValue()) / 100.0;
        return c.getDiscountValue();
    }

    private ValidationResponse buildValidationError(String m) {
        return ValidationResponse.builder().valid(false).discount(0.0).finalPrice(0.0).message(m).build();
    }

    @Override @Cacheable(value = "coupons", key = "#code")
    public Coupon findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new RuntimeException("Not found"));
    }

    @Override public List<Coupon> findAll() { return repository.findAll(); }
    @Override public List<Coupon> getCouponsByUserId(String id) { return repository.findByAssignedUserId(id); }
    @Override public List<Coupon> getCouponsByCampaignId(String id) { return repository.findByCampaignId(id); }
    @Override public Map<String, Object> createCoupon(Coupon c) { return Map.of("id", repository.save(c).getId()); }

    @Override
    @Transactional
    public Map<String, Object> assignCouponToUser(String code, String userId) {
        Coupon coupon = findByCode(code);
        coupon.setAssignedUserId(userId);
        repository.save(coupon);
        return Map.of("success", true, "message", "Assigned to " + userId);
    }

    @Override
    @Transactional
    public Map<String, Object> generateReferralCoupon(String referrerId) {
        String code = "REF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        repository.save(Coupon.builder().code(code).discountType("PERCENT").discountValue(10.0).isActive(true).build());
        return Map.of("success", true, "code", code);
    }

    @Override
    @CacheEvict(value = "coupons", allEntries = true)
    public Map<String, Object> createCouponFromMap(Map<String, Object> data) {
        Coupon saved = repository.save(Coupon.builder()
                .code((String) data.get("code"))
                .discountValue(Double.valueOf(data.get("discountValue").toString()))
                .discountType((String) data.get("discountType"))
                .startTime(data.containsKey("startTime") ? LocalDateTime.parse(data.get("startTime").toString()) : LocalDateTime.now())
                .endTime(data.containsKey("endTime") ? LocalDateTime.parse(data.get("endTime").toString()) : null)
                .isActive(true)
                .usedCount(0)
                .build());
        return Map.of("success", true, "id", saved.getId());
    }
}
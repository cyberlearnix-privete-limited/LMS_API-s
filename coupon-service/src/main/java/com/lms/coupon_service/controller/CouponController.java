package com.lms.coupon_service.controller;

import com.lms.coupon_service.dto.*;
import com.lms.coupon_service.entity.Coupon;
import com.lms.coupon_service.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService service;

    @PostMapping("/create/{role}")
    public ResponseEntity<Map<String, Object>> create(
            @PathVariable String role,
            @RequestBody Map<String, Object> data) {

        data.put("creatorRole", role.toUpperCase());

        if (!data.containsKey("startTime") || !data.containsKey("endTime")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Both startTime and endTime are required for coupon creation.",
                    "success", false
            ));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createCouponFromMap(data));
    }

    @PostMapping("/bulk-generate/{role}")
    public ResponseEntity<BulkResponse> bulk(
            @PathVariable String role,
            @RequestBody BulkRequest r) {

        r.setCreatorRole(role.toUpperCase());
        return ResponseEntity.ok(service.bulkGenerate(r));
    }

    @GetMapping("/{code}")
    public ResponseEntity<Coupon> getDetails(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(@RequestBody ValidateRequest r) {
        return ResponseEntity.ok(service.validateCoupon(r));
    }

    @PostMapping("/redeem")
    public ResponseEntity<Map<String, Object>> redeem(@RequestBody RedeemRequest r) {
        return ResponseEntity.ok(service.redeemCoupon(r));
    }

    @PostMapping("/auto-apply")
    public ResponseEntity<ValidationResponse> autoApply(@RequestBody AutoApplyRequest r) {
        return ResponseEntity.ok(service.autoApply(r));
    }

    @PostMapping("/user-coupons/assign")
    public ResponseEntity<Map<String, Object>> assign(@RequestBody Map<String, String> r) {
        return ResponseEntity.ok(service.assignCouponToUser(r.get("code"), r.get("userId")));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Coupon>> getUserCoupons(@PathVariable String userId) {
        return ResponseEntity.ok(service.getCouponsByUserId(userId));
    }

    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<Coupon>> getCampaignCoupons(@PathVariable String campaignId) {
        return ResponseEntity.ok(service.getCouponsByCampaignId(campaignId));
    }

    // FIXED: Removed boolean assignment. Service throws exception if not found.
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String id) {
        // Call the service. If it fails, the GlobalExceptionHandler takes over.
        service.deleteById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Coupon deleted successfully",
                "id", id
        ));
    }
    @PostMapping("/referrals/generate")
    public ResponseEntity<Map<String, Object>> referral(@RequestBody ReferralRequest r) {
        return ResponseEntity.ok(service.generateReferralCoupon(r.getReferrerId()));
    }
}
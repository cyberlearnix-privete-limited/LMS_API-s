package com.lms.coupon_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_usage_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String couponCode;

    // Added to replace Order ID for enrollment tracking
    @Column(nullable = false)
    private String courseId;

    @Column(nullable = false)
    private LocalDateTime usedAt;
}
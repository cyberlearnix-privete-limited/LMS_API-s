package com.lms.coupon_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String code;

    // --- IMPORTANT: Ensure these are not null in the DB ---
    private String discountType; // Should be "PERCENT" or "FIXED"
    private Double discountValue; // Should be e.g., 20.0
    // -----------------------------------------------------

    private Double minPurchase;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expiryDate;

    private Integer usageLimit;
    private Integer perUserLimit;

    @Builder.Default
    private Integer usedCount = 0;

    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coupon_courses", joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "course_id")
    private List<String> courses;

    private String assignedUserId;
    private String campaignId;
}
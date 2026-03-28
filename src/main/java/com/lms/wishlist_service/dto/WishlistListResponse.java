package com.lms.wishlist_service.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistListResponse {
    private String userId;
    private Integer totalItems;
    private List<WishlistResponse> items;
}
package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    // This refers to the CartItem in the same 'dto' package
    private List<CartItem> items;
    private Double totalCartPrice;
}
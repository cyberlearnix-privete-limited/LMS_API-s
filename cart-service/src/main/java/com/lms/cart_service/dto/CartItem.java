package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Long cartId;        // Unique ID for this specific row in the DB
    private String instructorId;
    private String courseId;
    private String courseName;
    private Double unitPrice;
    private Integer quantity;   // Managed by the "Minase" logic
    private Double subTotal;    // (unitPrice * quantity)
}
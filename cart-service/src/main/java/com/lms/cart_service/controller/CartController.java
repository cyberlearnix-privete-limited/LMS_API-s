package com.lms.cart_service.controller;

import com.lms.cart_service.dto.ApiResponse;
import com.lms.cart_service.dto.CartRequest;
import com.lms.cart_service.dto.CartResponse;
import com.lms.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @PathVariable String userId, @RequestBody CartRequest request) {
        CartResponse data = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added and cart updated", data));
    }

    @GetMapping("/{userId}/view")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable String userId) {
        CartResponse data = cartService.getUserCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", data));
    }

    @DeleteMapping("/{userId}/remove-item")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable String userId, @RequestParam Long cartId,
            @RequestParam String instructorId, @RequestParam String courseId) {
        CartResponse data = cartService.removeFromCart(userId, cartId, instructorId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Cart updated successfully", data));
    }

    @DeleteMapping("/{userId}/clear-all")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(@PathVariable String userId) {
        CartResponse data = cartService.clearFullCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", data));
    }
}
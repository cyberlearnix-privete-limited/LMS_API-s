package com.lms.cart_service.service;

import com.lms.cart_service.dto.CartRequest;
import com.lms.cart_service.dto.CartResponse;

public interface CartService {
    CartResponse addToCart(String userId, CartRequest request);
    CartResponse getUserCart(String userId);
    CartResponse removeFromCart(String userId, Long cartId, String instructorId, String courseId);
    CartResponse clearFullCart(String userId);
}
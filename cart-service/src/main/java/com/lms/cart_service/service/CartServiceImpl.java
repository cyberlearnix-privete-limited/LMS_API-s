package com.lms.cart_service.service;

import com.lms.cart_service.dto.CartRequest;
import com.lms.cart_service.dto.CartResponse;
import com.lms.cart_service.entity.CartItem;
import com.lms.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    @Transactional
    public CartResponse addToCart(String userId, CartRequest request) {
        // 1. Check if item already exists in the database
        var existing = cartRepository.findByUserIdAndCourseIdAndInstructorId(
                userId, request.getCourseId(), request.getInstructorId());

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + 1);
            cartRepository.save(item);
        } else {
            // 2. DISCONNECTED LOGIC: No external API calls
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setCourseId(request.getCourseId());
            newItem.setInstructorId(request.getInstructorId());

            // Use data directly from the Request Body (Postman)
            newItem.setCourseName(request.getCourseName());
            newItem.setPrice(request.getPrice());

            newItem.setQuantity(1);
            cartRepository.save(newItem);
        }
        return getUserCart(userId);
    }

    @Override
    public CartResponse getUserCart(String userId) {
        List<CartItem> entities = cartRepository.findAllByUserId(userId);

        List<com.lms.cart_service.dto.CartItem> dtos = entities.stream().map(entity -> {
            com.lms.cart_service.dto.CartItem dto = new com.lms.cart_service.dto.CartItem();
            dto.setCartId(entity.getId());
            dto.setInstructorId(entity.getInstructorId());
            dto.setCourseId(entity.getCourseId());
            dto.setCourseName(entity.getCourseName());
            dto.setUnitPrice(entity.getPrice());
            dto.setQuantity(entity.getQuantity());
            dto.setSubTotal(entity.getPrice() * entity.getQuantity());
            return dto;
        }).toList();

        Double grandTotal = dtos.stream().mapToDouble(d -> d.getSubTotal()).sum();
        return new CartResponse(dtos, grandTotal);
    }

    @Override
    @Transactional
    public CartResponse removeFromCart(String userId, Long cartId, String instructorId, String courseId) {
        CartItem item = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartRepository.save(item);
        } else {
            cartRepository.delete(item);
        }
        return getUserCart(userId);
    }

    @Override
    @Transactional
    public CartResponse clearFullCart(String userId) {
        cartRepository.deleteByUserId(userId);
        return new CartResponse(new ArrayList<>(), 0.0);
    }
}
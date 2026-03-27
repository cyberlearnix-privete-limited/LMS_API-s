package com.lms.cart_service.repository;

import com.lms.cart_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    // Requirement 3: Get all items for the logged-in user
    List<CartItem> findAllByUserId(String userId);

    // Requirement 1 & 2: Find specific item to check if we should increment or decrement quantity
    Optional<CartItem> findByUserIdAndCourseIdAndInstructorId(String userId, String courseId, String instructorId);

    // Requirement 4: Delete all items for a specific user
    void deleteByUserId(String userId);
}
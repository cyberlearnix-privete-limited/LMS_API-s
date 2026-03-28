package com.lms.wishlist_service.service;

import com.lms.wishlist_service.client.CourseClient;
import com.lms.wishlist_service.dto.*;
import com.lms.wishlist_service.entity.WishlistItem;
import com.lms.wishlist_service.exception.WishlistException;
import com.lms.wishlist_service.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository repository;
    private final CourseClient courseClient;

    /**
     * Adds an item to the database.
     * Throws WishlistException (409 CONFLICT) if duplicate is found.
     */
    public WishlistResponse addToWishlist(String userId, String courseId) {
        if (repository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            throw new WishlistException("Course is already in your wishlist", HttpStatus.CONFLICT);
        }

        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .courseId(courseId)
                .build();

        WishlistItem savedItem = repository.save(item);
        return mapToWishlistResponse(savedItem);
    }

    /**
     * Retrieves all items for a specific user.
     */
    public WishlistListResponse getWishlist(String userId) {
        List<WishlistItem> entities = repository.findByUserId(userId);

        List<WishlistResponse> responses = entities.stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());

        return WishlistListResponse.builder()
                .userId(userId)
                .totalItems(responses.size())
                .items(responses)
                .build();
    }

    public boolean checkExists(String userId, String courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId).isPresent();
    }

    @Transactional
    public void removeFromWishlist(String userId, String courseId) {
        if (!checkExists(userId, courseId)) {
            throw new WishlistException("Course not found in wishlist", HttpStatus.NOT_FOUND);
        }
        repository.deleteByUserIdAndCourseId(userId, courseId);
    }

    @Transactional
    public String clearWishlist(String userId) {
        List<WishlistItem> items = repository.findByUserId(userId);

        if (items.isEmpty()) {
            return "Wishlist was already empty";
        }

        repository.deleteByUserId(userId);
        return "Wishlist cleared successfully";
    }

    @Transactional
    public void moveToCart(String userId, String courseId) {
        if (!checkExists(userId, courseId)) {
            throw new WishlistException("Course not found in your wishlist", HttpStatus.NOT_FOUND);
        }
        // TODO: Call CartClient here in next phase
        removeFromWishlist(userId, courseId);
    }

    /**
     * Maps the DB Entity to the nested DTO structure.
     */
    private WishlistResponse mapToWishlistResponse(WishlistItem entity) {
        CourseDetails mockDetails = CourseDetails.builder()
                .courseId(entity.getCourseId())
                .title("Introduction to Cybersecurity")
                .price(BigDecimal.ZERO)
                .currency("INR")
                .category("Database Test")
                .build();

        return WishlistResponse.builder()
                .wishlistId(entity.getId())
                .userId(entity.getUserId())
                .addedAt(entity.getAddedAt())
                .course(mockDetails)
                .build();
    }
}
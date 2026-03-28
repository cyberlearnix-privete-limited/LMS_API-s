package com.lms.cart_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;       // Extracted from JWT Token
    private String instructorId; // From Request Body
    private String courseId;     // From Request Body
    private String courseName;
    private Double price;
    private Integer quantity;    // Used for the "Minase" (Minus) logic
}
package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyResponse;
import com.user.register.dto.UserProfileResponse;
import com.user.register.service.InstructorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
@RestController
@RequestMapping("/instructors")
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<InstructorApplyResponse>> applyForInstructor(HttpServletRequest request) {

        try {
            // call service method that returns detailed user info
            InstructorApplyResponse responseData = instructorService.applyForInstructor(request);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Instructor application submitted successfully. Awaiting admin approval.",
                            responseData,
                            LocalDateTime.now()
                    )
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
}
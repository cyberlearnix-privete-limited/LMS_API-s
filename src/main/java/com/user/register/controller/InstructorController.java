package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.InstructorApplyResponse;
import com.user.register.service.InstructorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/instructor") // ✅ Match SecurityConfig request matcher
public class InstructorController {

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<InstructorApplyResponse>> applyForInstructor(HttpServletRequest request) {

        try {
            // ✅ Call service to apply for instructor
            InstructorApplyResponse responseData = instructorService.applyForInstructor(request);

            return ResponseEntity.ok(
                    new ApiResponse<>(
                            true,
                            "Instructor application submitted successfully. Awaiting admin approval.",
                            responseData,
                            LocalDateTime.now()
                    )
            );

        } catch (ResponseStatusException e) {
            // ✅ Handle service-level 401 / 403 / 400 properly
            HttpStatus status = (HttpStatus) e.getStatusCode();
            return ResponseEntity.status(status)
                    .body(new ApiResponse<>(false, e.getReason(), null, LocalDateTime.now()));

        } catch (RuntimeException e) {
            // fallback for unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));
        }
    }
}